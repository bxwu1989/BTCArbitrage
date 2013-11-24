package exchange.currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


public class CurrencyQuantDelegate implements Comparable<CurrencyQuantDelegate> {

	private static final RoundingMode PRESENTATION_AND_REPORTING_ROUNDING_MODE = RoundingMode.HALF_UP;  //  Standard for reporting financial numbers  
	private static final int PRESENTATION_DECIMAL_PRECISION = 2; // easy on the eyes
	private static final RoundingMode CALCULATION_ROUNDING_MODE = RoundingMode.HALF_EVEN; // attempt to evenly distribute rounding between up and down
	private static final int CALCULATION_DECIMAL_PRECISION = 8;  // minimum resolution of Bitcoin is 0.00000001 BTC 
	private static final MathContext CALCULATION_CONTEXT = new MathContext(CALCULATION_DECIMAL_PRECISION, CALCULATION_ROUNDING_MODE);
	
	private final BigDecimal quant;

	public static CurrencyQuantDelegate getCurrencyQuant(double val) {
		return new CurrencyQuantDelegate(new BigDecimal(val, CALCULATION_CONTEXT));
	}
	
	public CurrencyQuantDelegate multiply(BigDecimal b) {
		return new CurrencyQuantDelegate(quant.multiply(b, CALCULATION_CONTEXT));
	}
	
	public CurrencyQuantDelegate multiply(CurrencyQuantDelegate b) {
		return new CurrencyQuantDelegate(quant.multiply(b.quant, CALCULATION_CONTEXT));
	}
	
	public CurrencyQuantDelegate divide(BigDecimal b) {
		return new CurrencyQuantDelegate(quant.divide(b, CALCULATION_CONTEXT));
	}
	
	public CurrencyQuantDelegate divide(CurrencyQuantDelegate b) {
		return new CurrencyQuantDelegate(quant.divide(b.quant, CALCULATION_CONTEXT));
	}
	
	public CurrencyQuantDelegate add(BigDecimal b) {
		return new CurrencyQuantDelegate(quant.add(b));
	}
	
	public CurrencyQuantDelegate add(CurrencyQuantDelegate b) {
		return new CurrencyQuantDelegate(quant.add(b.quant));
	}
	
	public CurrencyQuantDelegate subtract(BigDecimal b) {
		return new CurrencyQuantDelegate(quant.subtract(b));
	}
	
	public CurrencyQuantDelegate subtract(CurrencyQuantDelegate b) {
		return new CurrencyQuantDelegate(quant.subtract(b.quant));
	}
	
	@Override 
	public int compareTo(CurrencyQuantDelegate compare) {
		return quant.compareTo(compare.quant);
	}
	
	@Override
	public String toString() {
		return quant.setScale(PRESENTATION_DECIMAL_PRECISION, PRESENTATION_AND_REPORTING_ROUNDING_MODE).toString();
	}
	
	private CurrencyQuantDelegate(BigDecimal quant) {
		this.quant = quant;
	}
	
	private CurrencyQuantDelegate(CurrencyQuantDelegate delegateQuant) {
		this.quant = delegateQuant.quant;
	}
}
