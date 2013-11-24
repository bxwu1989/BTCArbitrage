package exchange.paths;

import exchange.Exchange;
import exchange.InnerExchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;
import exchange.paths.Path.PathBuilder;

public class PathLoader {

	private static final CurrencyQuantDelegate STARTING_PATH_QUANTITY = CurrencyQuantDelegate.getCurrencyQuant(100);
	private static final int PATH_DEPTH_LIMIT = 10;

	public static void loadPaths() {
		for (Exchange ex : Exchange.values()) {
			for (InnerExchange innerEx : ex.getInnerExchangesForConvertingWith(CurrencyType.Fiat)) {
				final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getBuySellExchangeActionNode(ex, innerEx);
				buildPaths( Path.builder(PATH_DEPTH_LIMIT, exActionNode) );
			}
		}
	}
	
	private static void buildPaths(PathBuilder pathBuilder) {		
		final ExchangeActionNode previousNode = pathBuilder.getLastExchangeActionNode();
		final Currency currentCurrency = previousNode.getDestinationCurrency();
		final Exchange ex = previousNode.getExchange();
	
		switch (currentCurrency.getType()) { 
		case Digital: // get transfer options if digital currency
			if (!currentCurrency.equals(previousNode.getSourceCurrency())) { // check to see if the last action was a transfer, 
																			 // if so skip because it doesn't makes any sense to transfer again
				for (Exchange destinationExchange : ex.getDestinationExchangesForCurrency(currentCurrency)) {
					final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getTransferExchangeActionNode(destinationExchange, currentCurrency);
					buildPaths( pathBuilder.addNode(exActionNode) );			
				}
			}
		case Fiat: // If the path is currently holding the same currency as the starting currency, 
					// and the path contains at least 3 actions then we can close off the path.
					// We don't allow only 2 actions because that would mean it only consists of a
					// buy and sell in the same exchange.
			if (pathBuilder.checkIfEndOfPathReachedWithNode() && pathBuilder.getSize() > 2) {
				pathBuilder.buildAndRegisterWithExchangesInnerExchanges(STARTING_PATH_QUANTITY);
			}
			default: // Add all buy/sell action possible				 
				for (InnerExchange innerEx : ex.getInnerExchangesForConvertingWith(currentCurrency)) {
					final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getBuySellExchangeActionNode(ex, innerEx);
					final PathBuilder newPathBuilder = pathBuilder.addNodeIgnoreDuplicatesAndRespectDepth(exActionNode);
					if (newPathBuilder != null) {
						buildPaths(newPathBuilder);
					}				
				}
				break;					
		}
	}
}
