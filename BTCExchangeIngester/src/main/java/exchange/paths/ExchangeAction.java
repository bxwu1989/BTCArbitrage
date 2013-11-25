package exchange.paths;

import exchange.Exchange;
import exchange.currency.Currency;
import exchange.currency.QuantityDelegate;


public interface ExchangeAction {

	public QuantityDelegate getAppliedFeeQuantity(QuantityDelegate originalQuantity);
	public QuantityDelegate getConvertedQuantity(QuantityDelegate originalQuantity); //amount after fees and or currency conversion
	public Exchange getExchange();
	public Currency getSourceCurrency();
	public Currency getDestinationCurrency();

}
