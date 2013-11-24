package exchange.currency;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum Currency {

	USD(CurrencyType.Fiat),
	
	BTC(CurrencyType.Crypto),
	LTC(CurrencyType.Crypto),
	
	XRP(CurrencyType.Non_Crypto);
	
	private final CurrencyType type;
	
	private Currency (CurrencyType type) {
		this.type = type;
	}

	public CurrencyType getType() {
		return type;
	}
	
	private final static Map<CurrencyType, Set<Currency>> typeMap = new EnumMap<>(CurrencyType.class);
	static {
		for (Currency currency : values()) {
			Set<Currency> currenciesForType = typeMap.get(currency.getType());
			if (currenciesForType == null) {
				currenciesForType = EnumSet.of(currency);
				typeMap.put(currency.getType(), currenciesForType);
			} else {
				currenciesForType.add(currency);
			}
		}
	}

	public static Set<Currency> getCurrenciesOfType(CurrencyType type) {
		return typeMap.get(type);
	}
}
