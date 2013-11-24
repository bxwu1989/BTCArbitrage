package exchange;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import exchange.currency.Currency;

public enum InnerExchange {
	// Intended to serve as a directional exchange definition because not all
	// exchanges will offer the ability to trade in all directions for two or
	// more currencies.
	USD_BTC(Currency.USD, Currency.BTC),
	USD_LTC(Currency.USD, Currency.LTC),
	
	BTC_USD(Currency.BTC, Currency.USD),
	BTC_LTC(Currency.BTC, Currency.LTC),
	
	LTC_USD(Currency.LTC, Currency.USD),
	LTC_BTC(Currency.LTC, Currency.BTC);
	
	private final Currency sourceCurrency;
	private final Currency destinationCurrency;

	private InnerExchange(Currency bid, Currency ask) {
		this.sourceCurrency = bid;
		this.destinationCurrency = ask;
	}

	public Currency getSourceCurrency() {
		return sourceCurrency;
	}

	public Currency getDestinationCurrency() {
		return destinationCurrency;
	}
	
	public List<Currency> getCurrencies() {
		return ImmutableList.<Currency>builder().add(sourceCurrency, destinationCurrency).build();
	}
	
	public static InnerExchange getInnerExchange(Currency source, Currency destination) { 
		final Iterator<InnerExchange> iter = Sets.intersection(innerExchangesForSourceCurrency.get(source), innerExchangesForDestinationCurrency.get(destination)).iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		return null;
	}

	private static Map<Currency, Set<InnerExchange>> innerExchangesForSourceCurrency = new EnumMap<>(Currency.class);
	private static Map<Currency, Set<InnerExchange>> innerExchangesForDestinationCurrency = new EnumMap<>(Currency.class);
	private static Map<Currency, Set<InnerExchange>> innerExchangesForCurrency = new EnumMap<>(Currency.class);
	static {
		for (InnerExchange innerEx : values()) {
			final Currency sourceCurrency = innerEx.getSourceCurrency();
			Set<InnerExchange> innerExsForSource = innerExchangesForSourceCurrency.get(sourceCurrency);
			if (innerExsForSource == null) {
				innerExsForSource = EnumSet.noneOf(InnerExchange.class);
				innerExchangesForSourceCurrency.put(sourceCurrency, innerExsForSource);
			}
			innerExsForSource.add(innerEx);
			
			final Currency destinationCurrency = innerEx.getDestinationCurrency();
			Set<InnerExchange> innerExsForDestination = innerExchangesForDestinationCurrency.get(destinationCurrency);
			if (innerExsForDestination == null) {
				innerExsForDestination = EnumSet.noneOf(InnerExchange.class);
				innerExchangesForDestinationCurrency.put(destinationCurrency, innerExsForDestination);
			}
			innerExsForDestination.add(innerEx);
			
			innerExsForSource = innerExchangesForCurrency.get(sourceCurrency);
			if (innerExsForSource == null) {
				innerExsForSource = EnumSet.noneOf(InnerExchange.class);
				innerExchangesForCurrency.put(sourceCurrency, innerExsForSource);
			}
			innerExsForSource.add(innerEx);
			innerExsForDestination = innerExchangesForCurrency.get(destinationCurrency);
			if (innerExsForDestination == null) {
				innerExsForDestination = EnumSet.noneOf(InnerExchange.class);
				innerExchangesForCurrency.put(destinationCurrency, innerExsForDestination);
			}
			innerExsForDestination.add(innerEx);
		}
	}
	
	public static Set<InnerExchange> getInnerExchangesForCurrenciesIntersection(Set<Currency> currencies) {
		final Set<InnerExchange> innerExsForCurrencies = EnumSet.noneOf(InnerExchange.class);
		for (Currency currency : currencies) {
			for (Currency others : Sets.difference(currencies, EnumSet.of(currency))) {
				innerExsForCurrencies.addAll( Sets.intersection(innerExchangesForCurrency.get(currency), innerExchangesForCurrency.get(others)) );
			}
		}
		return innerExsForCurrencies;
	}
	
	public int getDirectionlessHashCode() {
		return sourceCurrency.hashCode() + destinationCurrency.hashCode();
	}
}
