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
import exchange.currency.CurrencyType;

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
	
	public static Set<InnerExchange> getInnerExchangesDirectionless(Currency c1, Currency c2) { 
		return Sets.union(Sets.intersection(innerExchangesForSourceCurrency.get(c1), innerExchangesForDestinationCurrency.get(c2)), 
				Sets.intersection(innerExchangesForSourceCurrency.get(c2), innerExchangesForDestinationCurrency.get(c1)));
	}

	public static Set<InnerExchange> getInnerExchangesForConvertingWith(Currency source) {
		return innerExchangesForSourceCurrency.get(source);
	}
	
	public static Set<InnerExchange> getInnerExchangesForConvertingWith(CurrencyType source) {
		return innerExchangesForSourceCurrencyType.get(source);
	}
	
	public static Set<InnerExchange> getAllExchangesContainingCurrency(Currency currency) {
		return Sets.union(innerExchangesForSourceCurrency.get(currency), innerExchangesForDestinationCurrency.get(currency));
	}
	
	public static Set<InnerExchange> getAllInnerExchangeCombinationsForCurrencies(Set<Currency> currencies) {
		final Set<InnerExchange> innerExsForCurrencies = EnumSet.noneOf(InnerExchange.class);
		for (Currency currency : currencies) {
			final Set<InnerExchange> allInnerExchangesForCurrency = getAllExchangesContainingCurrency(currency);
			for (Currency compareToCurrency : Sets.difference(currencies, EnumSet.of(currency))) {
				innerExsForCurrencies.addAll( Sets.intersection(allInnerExchangesForCurrency, getAllExchangesContainingCurrency(compareToCurrency)) );
			}
		}
		return innerExsForCurrencies;
	}
	
	private static final Map<Currency, Set<InnerExchange>> innerExchangesForSourceCurrency = new EnumMap<>(Currency.class);
	private static final Map<Currency, Set<InnerExchange>> innerExchangesForDestinationCurrency = new EnumMap<>(Currency.class);
	private static final Map<CurrencyType, Set<InnerExchange>> innerExchangesForSourceCurrencyType = new EnumMap<>(CurrencyType.class);
	private static final Map<CurrencyType, Set<InnerExchange>> innerExchangesForDestinationCurrencyType = new EnumMap<>(CurrencyType.class);
	
	static {
		for (Currency currency : Currency.values()) { // init maps
			innerExchangesForSourceCurrency.put(currency, EnumSet.noneOf(InnerExchange.class));
			innerExchangesForDestinationCurrency.put(currency, EnumSet.noneOf(InnerExchange.class));
			innerExchangesForSourceCurrencyType.put(currency.getType(), EnumSet.noneOf(InnerExchange.class));
			innerExchangesForDestinationCurrencyType.put(currency.getType(), EnumSet.noneOf(InnerExchange.class));
		}
		
		for (InnerExchange innerEx : values()) {
			final Currency sourceCurrency = innerEx.getSourceCurrency();
			innerExchangesForSourceCurrency.get(sourceCurrency).add(innerEx);
			innerExchangesForSourceCurrencyType.get(sourceCurrency.getType()).add(innerEx);
			
			final Currency destinationCurrency = innerEx.getDestinationCurrency();			
			innerExchangesForDestinationCurrency.get(destinationCurrency).add(innerEx);			
			innerExchangesForDestinationCurrencyType.get(destinationCurrency.getType()).add(innerEx);
		}
	}
	
	public int getDirectionlessHashCode() {
		return sourceCurrency.hashCode() + destinationCurrency.hashCode();
	}
}
