package exchange.paths;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import exchange.InnerExchange;
import exchange.client.Exchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;

public class BuySellExchangeActionNode implements ExchangeActionNode {

	private final InnerExchange innerExchange;
	private final Exchange outerExchange;

	BuySellExchangeActionNode(Exchange outerEx, InnerExchange innerEx) {
		this.innerExchange = innerEx;
		this.outerExchange = outerEx;
	}
	
	@Override
	public CurrencyQuantDelegate getAppliedFeeQuantity(CurrencyQuantDelegate originalQuantity) {
		return outerExchange.getAppliedTradeCommissionFeeQuantity(originalQuantity, innerExchange);
	}

	@Override
	public CurrencyQuantDelegate getConvertedQuantity(CurrencyQuantDelegate originalQuantity) {
		return outerExchange.convertAmount(originalQuantity, innerExchange);
	}

	@Override
	public Exchange getExchange() {
		return outerExchange;
	}

	@Override
	public Currency getSourceCurrency() {
		return innerExchange.getSourceCurrency();
	}

	@Override
	public Currency getDestinationCurrency() {
		return innerExchange.getDestinationCurrency();
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
