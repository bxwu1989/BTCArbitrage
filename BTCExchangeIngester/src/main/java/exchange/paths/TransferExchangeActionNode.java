package exchange.paths;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import exchange.client.Exchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;

public class TransferExchangeActionNode implements ExchangeActionNode {

	private final Exchange outerExchange;
	private final Currency currency;
	
	TransferExchangeActionNode(Exchange outerEx, Currency currency) {
		this.outerExchange = outerEx;
		this.currency = currency;
	}
	
	@Override
	public CurrencyQuantDelegate getAppliedFeeQuantity(CurrencyQuantDelegate originalQuantity) {
		return outerExchange.getAppliedCurrencyTransferFeeQuantity(originalQuantity, currency);
	}

	@Override
	public CurrencyQuantDelegate getConvertedQuantity(CurrencyQuantDelegate originalQuantity) {
		return originalQuantity.subtract(getAppliedFeeQuantity(originalQuantity));
	}

	@Override
	public Exchange getExchange() {
		return outerExchange;
	}

	@Override
	public Currency getSourceCurrency() {
		return currency;
	}

	@Override
	public Currency getDestinationCurrency() {
		return currency;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this).toString();
	}
}
