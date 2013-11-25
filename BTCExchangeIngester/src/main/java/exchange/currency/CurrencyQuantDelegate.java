package exchange.currency;

import java.math.BigDecimal;
import java.math.MathContext;


class CurrencyQuantDelegate extends QuantityDelegate {

	private final Currency currency;
	
	@Override
	public QuantityDelegate multiply(QuantityDelegate b) {
		return new CurrencyQuantDelegate(quant.multiply(b.quant, currency.getCalculationMathContext()), currency);
	}
	
	@Override
	public QuantityDelegate divide(QuantityDelegate b) {
		return new CurrencyQuantDelegate(quant.divide(b.quant, currency.getCalculationMathContext()), currency);
	}
	
	@Override
	public QuantityDelegate add(QuantityDelegate b) {
		return new CurrencyQuantDelegate(quant.add(b.quant, currency.getCalculationMathContext()), currency);
	}
	
	@Override
	public QuantityDelegate subtract(QuantityDelegate b) {
		return new CurrencyQuantDelegate(quant.subtract(b.quant, currency.getCalculationMathContext()), currency);
	}
	
	@Override
	public String toString() {
		final MathContext currencyMathContext = currency.getPresentationMathContext();
		return quant.setScale(currencyMathContext.getPrecision(), currencyMathContext.getRoundingMode()).toString();
	}
	
	protected CurrencyQuantDelegate(double quant, Currency currency) {
		super(new BigDecimal(quant, currency.getCalculationMathContext()));
		this.currency = currency;
	}
	
	protected CurrencyQuantDelegate(BigDecimal quant, Currency currency) {
		super(quant);
		this.currency = currency;
	}
}
