package exchange.currency;

import java.math.MathContext;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum Currency {

	USD(CurrencyType.Fiat, 6, 2),
	
	BTC(CurrencyType.Digital, 8, 4),
	LTC(CurrencyType.Digital, 8, 4),
	
	XRP(CurrencyType.Digital, 6, 2);
	
	private final CurrencyType type;
	private final MathContext calculationMathContext;
	private final MathContext presentationMathContext;
	
	private Currency (CurrencyType type, int calculationDecimalPrecision, int presentationDecimalPrecision) {
		this.type = type;
		this.calculationMathContext = new MathContext(calculationDecimalPrecision, QuantityDelegate.DEFAULT_CALCULATION_ROUNDING_MODE);
		this.presentationMathContext = new MathContext(presentationDecimalPrecision, QuantityDelegate.DEFAULT_PRESENTATION_AND_REPORTING_ROUNDING_MODE); 
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

	public MathContext getCalculationMathContext() {
		return calculationMathContext;
	}

	public MathContext getPresentationMathContext() {
		return presentationMathContext;
	}
}
