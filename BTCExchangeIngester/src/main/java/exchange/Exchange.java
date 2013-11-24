package exchange;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import exchange.configuration.ExchangeExchangeConfig;
import exchange.configuration.ExchangeFeeConfig;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;
import exchange.paths.Path;
import exchange.services.response.MarketDepth;
import exchange.services.response.Ticker;

public enum Exchange {
	CAMPBX,
	BITFINIX,
	BITSTAMP;

	// Active Exchange Data
	private final Map<Integer, MarketDepth> innerExchangeMarketDepths = new ConcurrentHashMap<Integer, MarketDepth>();	
	private final Ticker ticker = new Ticker();
	
	private Exchange() {	
	}
	
	public void updateMarketDepth(Currency source, Currency dest, MarketDepth marketDepth) {
		final InnerExchange innerEx = ExchangeExchangeConfig.getInnerExchange(this, source, dest);
		innerExchangeMarketDepths.put(innerEx.getDirectionlessHashCode(), marketDepth);
		updatePaths(this, innerEx);
	}
	
	public void updateTicker(Ticker ticker) {
		this.ticker.updateTicker(ticker);
	}
	
	public CurrencyQuantDelegate getAppliedTradeCommissionFeeQuantity(CurrencyQuantDelegate quantity, InnerExchange innerExchange) {
		return ExchangeFeeConfig.getTradeCommissionFee(this, innerExchange).getAppliedFeeQuantity(quantity);
	}
	
	public CurrencyQuantDelegate convertAmount(CurrencyQuantDelegate quantity, InnerExchange innerExchange) {
		final MarketDepth marketDepth = innerExchangeMarketDepths.get(innerExchange.getDirectionlessHashCode());
		if (marketDepth == null) return CurrencyQuantDelegate.getCurrencyQuant(0);
		
		CurrencyQuantDelegate convertedQuant = ExchangeFeeConfig.getTradeCommissionFee(this, innerExchange).getQuantityAfterAppliedFee(quantity);
		switch (innerExchange.getSourceCurrency().getType()) {
		case Fiat:
			convertedQuant = marketDepth.consumeAsks(convertedQuant);
			break;
		case Digital:
			convertedQuant = marketDepth.consumeBids(convertedQuant);
			break;
		}
		return convertedQuant;
	}
	
	public CurrencyQuantDelegate getAppliedCurrencyTransferFeeQuantity(CurrencyQuantDelegate quantity, Currency currency) {
		return ExchangeFeeConfig.getTransferFee(this, currency).getAppliedFeeQuantity(quantity);
	}
	
	public Set<InnerExchange> getInnerExchangesForConvertingWith(CurrencyType sourceCurrencyType) {
		return ExchangeExchangeConfig.getInnerExchangesForConvertingWith(this, sourceCurrencyType);
	}
	
	public Set<InnerExchange> getInnerExchangesForConvertingWith(Currency sourceCurrency) {
		return ExchangeExchangeConfig.getInnerExchangesForConvertingWith(this, sourceCurrency);
	}
	
	public Set<Exchange> getDestinationExchangesForCurrency(Currency currency) {
		if (!currency.getType().equals(CurrencyType.Digital)) throw new IllegalArgumentException("Only digital currencies are allowed for transfering. Can not transfer currency " + currency);
		return Sets.difference(ExchangeExchangeConfig.getExchangesForCurrency(currency), EnumSet.of(this));
	}

	private static final Map<Exchange, Map<Integer, Set<Path>>> exchangeInnerExchangePathReferences;
	static {
		ImmutableMap.Builder<Exchange, Map<Integer, Set<Path>>> exchangeInnerExchangePathReferencesBuilder = ImmutableMap.builder();
		for (Exchange ex : Exchange.values()) {
			Map<Integer, Set<Path>> innerExchangePathReferences = new HashMap<>();
			for (InnerExchange innerEx : ExchangeExchangeConfig.getInnerExchanges(ex)) {
				innerExchangePathReferences.put(innerEx.getDirectionlessHashCode(), new HashSet<Path>());
			}

			exchangeInnerExchangePathReferencesBuilder.put(ex, innerExchangePathReferences);
		}
		exchangeInnerExchangePathReferences = exchangeInnerExchangePathReferencesBuilder.build();
	}
	
	private static void updatePaths(Exchange ex, InnerExchange innerEx) {
		for (Path path : exchangeInnerExchangePathReferences.get(ex).get(innerEx.getDirectionlessHashCode())) {
			path.updateFinalQuantity();
		}
	}
	
	public static void registerPathsWithExchangesInnerExchanges(Path path, Map<Exchange, Set<InnerExchange>> innerExchangesInPath) {
		for (final Entry<Exchange, Set<InnerExchange>> entry : innerExchangesInPath.entrySet()) {
			for (final InnerExchange innerEx : entry.getValue()) {
				exchangeInnerExchangePathReferences.get(entry.getKey()).get(innerEx.getDirectionlessHashCode()).add(path);
			}
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
