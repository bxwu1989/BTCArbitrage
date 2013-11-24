package exchange.paths;

import exchange.Exchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;


public interface ExchangeActionNode {

	public CurrencyQuantDelegate getAppliedFeeQuantity(CurrencyQuantDelegate originalQuantity);
	public CurrencyQuantDelegate getConvertedQuantity(CurrencyQuantDelegate originalQuantity); //amount after fees and or currency conversion
	public Exchange getExchange();
	public Currency getSourceCurrency();
	public Currency getDestinationCurrency();

}
