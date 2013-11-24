package exchange.paths;

import java.util.EnumMap;
import java.util.Map;

import exchange.InnerExchange;
import exchange.client.Exchange;
import exchange.currency.Currency;

public class ExchangeActionNodeFactory {

	private ExchangeActionNodeFactory() {}
	
	private static final Map<Exchange, Map<InnerExchange, ExchangeActionNode>> buySellExActionNodes = new EnumMap<>(Exchange.class);
	private static final Map<Exchange, Map<Currency, ExchangeActionNode>> transferExActionNodes = new EnumMap<>(Exchange.class);
	static {
		for (Exchange ex : Exchange.values()) {
			buySellExActionNodes.put(ex, new EnumMap<InnerExchange, ExchangeActionNode>(InnerExchange.class));
			transferExActionNodes.put(ex, new EnumMap<Currency, ExchangeActionNode>(Currency.class));
		}
	}

	public static ExchangeActionNode getBuySellExchangeActionNode(Exchange outerEx, InnerExchange innerEx) {
		final Map<InnerExchange, ExchangeActionNode> actionTypeInstanceMap = buySellExActionNodes.get(outerEx);
		ExchangeActionNode exActionNode = actionTypeInstanceMap.get(innerEx);
		if (exActionNode == null) {
			exActionNode = new BuySellExchangeActionNode(outerEx, innerEx);
			actionTypeInstanceMap.put(innerEx, exActionNode);
		}
		
		return exActionNode;
	}
	
	public static ExchangeActionNode getTransferExchangeActionNode(Exchange outerEx, Currency transferCurrency) {
		final Map<Currency, ExchangeActionNode> actionTypeInstanceMap = transferExActionNodes.get(outerEx);
		ExchangeActionNode exActionNode = actionTypeInstanceMap.get(transferCurrency);
		if (exActionNode == null) {
			exActionNode = new TransferExchangeActionNode(outerEx, transferCurrency);
			actionTypeInstanceMap.put(transferCurrency, exActionNode);
		}
		
		return exActionNode;
	}
}
