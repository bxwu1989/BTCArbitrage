package exchange.client;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Sets;

import exchange.InnerExchange;
import exchange.client.response.MarketDepth;
import exchange.client.response.Ticker;
import exchange.configuration.ExchangeApiConfig;
import exchange.configuration.ExchangeExchangeConfig;
import exchange.configuration.ExchangeFeeConfig;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;
import exchange.paths.Path;

public enum Exchange {
	CAMPBX,
	MTGOX;

	// Active Exchange Data
	private final Map<Integer, MarketDepth> innerExchangeDepths = new HashMap<Integer, MarketDepth>();
	
	private final Ticker ticker = new Ticker();
	
	private Exchange() {	
	}

	public String getBaseApiConnectionString() {
		return ExchangeApiConfig.getApiString(this);
	}
	
	public void updateBiDirectionalMarketDepth(Currency source, Currency dest, MarketDepth marketDepth) {
		innerExchangeDepths.put(
				ExchangeExchangeConfig.getInnerExchangesForCurrenciesIntersection(this,EnumSet.of(source, dest))
						.iterator().next().getDirectionlessHashCode(), marketDepth);
		updatePaths();	
	}
	
	public void updateTicker(Ticker ticker) {
		this.ticker.updateTicker(ticker);
	//	System.out.println("Updated " + this + this.ticker);
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
	
	private final Set<Path> pathsToUpdate = Sets.newSetFromMap(new ConcurrentHashMap<Path, Boolean>());
	public void registerPathForUpdate(Path path) {
		pathsToUpdate.add(path);
	}
	
	private void updatePaths() {
		for (Path path : pathsToUpdate) {
			path.updateFinalQuantity();
		}
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
	
}
