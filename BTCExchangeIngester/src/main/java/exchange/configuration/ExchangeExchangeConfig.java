package exchange.configuration;

import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import exchange.InnerExchange;
import exchange.client.Exchange;
import exchange.currency.Currency;

public class ExchangeExchangeConfig {

	
	private static final Map<Exchange, Set<InnerExchange>> INNER_EXCHANGES;
	static {
		ImmutableMap.Builder<Exchange, Set<InnerExchange>> builder = ImmutableMap.builder();
		builder.put(Exchange.CAMPBX, EnumSet.of(InnerExchange.USD_BTC, InnerExchange.BTC_USD));
		builder.put(Exchange.MTGOX, EnumSet.of(InnerExchange.USD_BTC, InnerExchange.BTC_USD));
		INNER_EXCHANGES = builder.build();
	}
	
	
	
	public static Set<InnerExchange> getInnerExchanges(Exchange ex) {
		return INNER_EXCHANGES.get(ex);
	}
	
	public static Set<Currency> getExchangeCurrencies(Exchange ex) {
		return EXCHANGE_CURRENCIES.get(ex);
	}
	
	public static Set<Exchange> getExchangesForCurrency(Currency c) {
		return EXCHANGES_FOR_CURRENCY.get(c);
	}
	
	public static Set<InnerExchange> getInnerExchangesForCurrenciesIntersection(Exchange ex, Set<Currency> currencies) {
		return Sets.intersection(INNER_EXCHANGES.get(ex), 
				InnerExchange.getInnerExchangesForCurrenciesIntersection(currencies));
	}
	
	private static final Map<Exchange, Set<Currency>> EXCHANGE_CURRENCIES;
	private static final Map<Currency, Set<Exchange>> EXCHANGES_FOR_CURRENCY;
	static {
		final ImmutableMap.Builder<Exchange, Set<Currency>> builder = ImmutableMap.builder();
		for (Entry<Exchange, Set<InnerExchange>> entry : INNER_EXCHANGES.entrySet()) {
			final Set<Currency> currencies = EnumSet.noneOf(Currency.class);
			for (InnerExchange innerEx : entry.getValue()) {
				currencies.addAll(innerEx.getCurrencies());
			}
			builder.put(entry.getKey(), Sets.immutableEnumSet(currencies));			
		}
		EXCHANGE_CURRENCIES = builder.build();
		
		final ImmutableMap.Builder<Currency, Set<Exchange>> currencyExBuilder = ImmutableMap.builder();
		for (Currency currency : Currency.values()) {
			final Set<Exchange> exchanges = EnumSet.noneOf(Exchange.class);
			for (Entry<Exchange, Set<Currency>> entry : EXCHANGE_CURRENCIES.entrySet()) {
				if (entry.getValue().contains(currency)) { 
					exchanges.add(entry.getKey());
				}
			}
			currencyExBuilder.put(currency, Sets.immutableEnumSet(exchanges));
		}
		EXCHANGES_FOR_CURRENCY = currencyExBuilder.build();
	}	
	
	private ExchangeExchangeConfig() {}
}
