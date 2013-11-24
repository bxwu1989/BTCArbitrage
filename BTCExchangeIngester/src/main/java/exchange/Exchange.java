package exchange;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import exchange.services.response.MarketDepth;
import exchange.services.response.Ticker;
import exchange.configuration.ExchangeExchangeConfig;
import exchange.configuration.ExchangeFeeConfig;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;
import exchange.paths.Path;

public enum Exchange {
	CAMPBX,
	BITFINIX;

	// Active Exchange Data
	private final Map<Integer, MarketDepth> innerExchangeDepths = new HashMap<Integer, MarketDepth>();
	
	private final Ticker ticker = new Ticker();
	
	private Exchange() {	
	}
	
	public void updateMarketDepth(Currency source, Currency dest, MarketDepth marketDepth) {
		final Set<Path> paths = new HashSet<Path>();
		for (InnerExchange innerEx : ExchangeExchangeConfig.getInnerExchangesForCurrenciesIntersection(this,EnumSet.of(source, dest))) {
			paths.addAll(pathsToUpdate.get(innerEx));
			innerExchangeDepths.put(innerEx.getDirectionlessHashCode(), marketDepth);
		}

		updatePaths(paths);	
	}
	
	public void updateTicker(Ticker ticker) {
		this.ticker.updateTicker(ticker);
	}
	
	public CurrencyQuantDelegate getAppliedTradeCommissionFeeQuantity(CurrencyQuantDelegate quantity, InnerExchange innerExchange) {
		return ExchangeFeeConfig.getTradeCommissionFee(this, innerExchange).getAppliedFeeQuantity(quantity);
	}
	
	public CurrencyQuantDelegate convertAmount(CurrencyQuantDelegate quantity, InnerExchange innerExchange) {
		CurrencyQuantDelegate convertedQuant = null;
		final MarketDepth marketDepth = innerExchangeDepths.get(innerExchange.getDirectionlessHashCode());
		if (marketDepth == null) return CurrencyQuantDelegate.getCurrencyQuant(0);
		
		quantity = ExchangeFeeConfig.getTradeCommissionFee(this, innerExchange).getQuantityAfterAppliedFee(quantity);
		switch (innerExchange.getSourceCurrency().getType()) {
		case Fiat:
			convertedQuant = marketDepth.consumeAsks(quantity);
			break;
		case Non_Crypto:
		case Crypto:
			convertedQuant = marketDepth.consumeBids(quantity);
		}
		return convertedQuant;
	}
	
	public CurrencyQuantDelegate getAppliedCurrencyTransferFeeQuantity(CurrencyQuantDelegate quantity, Currency currency) {
		return ExchangeFeeConfig.getTransferFee(this, currency).getAppliedFeeQuantity(quantity);
	}
	
	public Set<InnerExchange> getInnerExchangesForConvertingWith(CurrencyType currencyType) {
		final Set<InnerExchange> innerExchangesForConvertingCurrencyType = EnumSet.noneOf(InnerExchange.class);
		for (InnerExchange innerExchange : ExchangeExchangeConfig.getInnerExchanges(this)) {
			if (innerExchange.getSourceCurrency().getType().equals(currencyType)) {
				innerExchangesForConvertingCurrencyType.add(innerExchange);
			}
		}
		return innerExchangesForConvertingCurrencyType;
	}
	
	public Set<InnerExchange> getInnerExchangesForConvertingWith(Currency currency) {
		final Set<InnerExchange> innerExchangesForConvertingCurrency = EnumSet.noneOf(InnerExchange.class);
		for (InnerExchange innerExchange : ExchangeExchangeConfig.getInnerExchanges(this)) {
			if (innerExchange.getSourceCurrency().equals(currency)) {
				innerExchangesForConvertingCurrency.add(innerExchange);
			}
		}
		return innerExchangesForConvertingCurrency;
	}
	
	public Set<Exchange> getDestinationExchangesForCurrency(Currency currency) {
		return Sets.difference(ExchangeExchangeConfig.getExchangesForCurrency(currency), EnumSet.of(this));
	}
	
	private final Map<InnerExchange, Set<Path>> pathsToUpdate = new ConcurrentHashMap<>();
	public void registerPathForUpdate(Path path, Set<InnerExchange> innerExchanges) {
		for (InnerExchange innerEx : innerExchanges) { 
			Set<Path> paths = pathsToUpdate.get(innerEx);
			if (paths == null) {
				paths = new HashSet<>();
				pathsToUpdate.put(innerEx, paths);
			}
			paths.add(path);
		}
	}
	
	private void updatePaths(Set<Path> paths) {
		for (Path path : paths) {
			path.updateFinalQuantity();
		}
	}
	
	public static Exchange fromString(String exchangeString) {
		return fromString.get(exchangeString);
	}
	private static final Map<String, Exchange> fromString;
	static {
		ImmutableMap.Builder<String, Exchange> builder = ImmutableMap.builder();
		for (Exchange ex : values()) {
			builder.put(ex.toString(), ex);
		}
		fromString = builder.build();
	}
}
