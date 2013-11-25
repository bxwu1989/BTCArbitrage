package exchange.currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class QuantityDelegate implements Comparable<QuantityDelegate> {

	public static final RoundingMode DEFAULT_PRESENTATION_AND_REPORTING_ROUNDING_MODE = RoundingMode.HALF_UP;  //  Standard for reporting financial numbers  
	private static final int DEFAULT_PRESENTATION_DECIMAL_PRECISION = 2; // easy on the eyes
	public static final RoundingMode DEFAULT_CALCULATION_ROUNDING_MODE = RoundingMode.HALF_EVEN; // attempt to evenly distribute rounding between up and down
	private static final int DEFAULT_CALCULATION_DECIMAL_PRECISION = 8;  // minimum resolution of Bitcoin is 0.00000001 BTC 
	private static final MathContext CALCULATION_CONTEXT = new MathContext(DEFAULT_CALCULATION_DECIMAL_PRECISION, DEFAULT_CALCULATION_ROUNDING_MODE);
	
	protected final BigDecimal quant;

	public static QuantityDelegate getQuant(double val) {
		return new QuantityDelegate(new BigDecimal(val, CALCULATION_CONTEXT));
	}
	
	public static CurrencyQuantDelegate getCurrencyQuant(QuantityDelegate quant, Currency currency) {
		return new CurrencyQuantDelegate(quant.quant, currency);
	}
	
	public static CurrencyQuantDelegate getCurrencyQuant(double val, Currency currency) {
		return new CurrencyQuantDelegate(val, currency);
	}
	
	public QuantityDelegate multiply(QuantityDelegate b) {
		return multiply(b.quant , CALCULATION_CONTEXT);
	}
/*	public QuantityDelegate multiply(BigDecimal b) {
		return multiply(b, CALCULATION_CONTEXT);
	}	*/
	protected QuantityDelegate multiply(BigDecimal b, MathContext mc) {
		return new QuantityDelegate(quant.multiply(b, mc));
	}
	
	public QuantityDelegate divide(QuantityDelegate b) {
		return divide(b.quant);
	}
	public QuantityDelegate divide(BigDecimal b) {
		return divide(b, CALCULATION_CONTEXT);
	}
	public QuantityDelegate divide(BigDecimal b, MathContext mc) {
		return new QuantityDelegate(quant.divide(b, mc));
	}

	public QuantityDelegate add(QuantityDelegate b) {
		return add(b.quant);
	}
	public QuantityDelegate add(BigDecimal b) {
		return add(b, CALCULATION_CONTEXT);
	}
	public QuantityDelegate add(BigDecimal b, MathContext mc) {
		return new QuantityDelegate(quant.add(b, mc));
	}
	
	public QuantityDelegate subtract(QuantityDelegate b) {
		return subtract(b.quant);
	}
	public QuantityDelegate subtract(BigDecimal b) {
		return subtract(b, CALCULATION_CONTEXT);
	}
	public QuantityDelegate subtract(BigDecimal b, MathContext mc) {
		return new QuantityDelegate(quant.subtract(b, mc));
	}
	
	@Override 
	public int compareTo(QuantityDelegate compare) {
		return quant.compareTo(compare.quant);
	}
	
	@Override
	public String toString() {
		return quant.setScale(DEFAULT_PRESENTATION_DECIMAL_PRECISION, DEFAULT_PRESENTATION_AND_REPORTING_ROUNDING_MODE).toString();
	}
	
	protected QuantityDelegate(BigDecimal quant) {
		this.quant = quant;
	}
	
	protected QuantityDelegate(QuantityDelegate delegateQuant) {
		this.quant = delegateQuant.quant;
	}
}
