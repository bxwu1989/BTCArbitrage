package exchange.paths;

import java.util.EnumMap;
import java.util.Map;

import exchange.Exchange;
import exchange.InnerExchange;
import exchange.currency.Currency;

public class ExchangeActionFactory {

	private ExchangeActionFactory() {}
	
	private static final Map<Exchange, Map<InnerExchange, ExchangeAction>> buySellExActions = new EnumMap<>(Exchange.class);
	private static final Map<Exchange, Map<Currency, ExchangeAction>> transferExActions = new EnumMap<>(Exchange.class);
	static {
		for (Exchange ex : Exchange.values()) {
			buySellExActions.put(ex, new EnumMap<InnerExchange, ExchangeAction>(InnerExchange.class));
			transferExActions.put(ex, new EnumMap<Currency, ExchangeAction>(Currency.class));
		}
	}

	public static ExchangeAction getBuySellExchangeAction(Exchange outerEx, InnerExchange innerEx) {
		final Map<InnerExchange, ExchangeAction> actionTypeInstanceMap = buySellExActions.get(outerEx);
		ExchangeAction exAction = actionTypeInstanceMap.get(innerEx);
		if (exAction == null) {
			exAction = new BuySellExchangeActionStrategy(outerEx, innerEx);
			actionTypeInstanceMap.put(innerEx, exAction);
		}
		
		return exAction;
	}
	
	public static ExchangeAction getTransferExchangeAction(Exchange outerEx, Currency transferCurrency) {
		final Map<Currency, ExchangeAction> actionTypeInstanceMap = transferExActions.get(outerEx);
		ExchangeAction exAction = actionTypeInstanceMap.get(transferCurrency);
		if (exAction == null) {
			exAction = new TransferExchangeActionStrategy(outerEx, transferCurrency);
			actionTypeInstanceMap.put(transferCurrency, exAction);
		}
		
		return exAction;
	}
}
