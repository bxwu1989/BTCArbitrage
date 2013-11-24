package exchange.paths;

import java.util.ArrayList;
import java.util.List;

import exchange.InnerExchange;
import exchange.client.Exchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;
import exchange.paths.Path.PathBuilder;

public class PathLoader {

	private static final CurrencyQuantDelegate STARTING_PATH_QUANTITY = CurrencyQuantDelegate.getCurrencyQuant(100);
	private static final int PATH_DEPTH_LIMIT = 10;
	
	public static void loadPaths() {
		buildPaths(null);
	}
	
	private static List<Path> buildPaths(PathBuilder pathBuilder) {
		final List<Path> paths = new ArrayList<>();
		
		if (pathBuilder == null) {
			// for each exchange create initial exchange action starting with FiatCurrencies
			for (Exchange ex : Exchange.values()) {
				for (InnerExchange innerEx : ex.getInnerExchangesForConvertingWith(CurrencyType.Fiat)) {
					final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getBuySellExchangeActionNode(ex, innerEx);
					paths.addAll(buildPaths( Path.builder(PATH_DEPTH_LIMIT, exActionNode) ));
				}
			}
		} else {
			final ExchangeActionNode previousNode = pathBuilder.getLastExchangeActionNode();
			final Currency currentCurrency = previousNode.getDestinationCurrency();
			final Exchange ex = previousNode.getExchange();
		
			switch (currentCurrency.getType()) { 
			case Crypto:
			case Non_Crypto: // get transfer options if digital currency
				if (!currentCurrency.equals(previousNode.getSourceCurrency())) {
					for (Exchange destinationExchange : ex.getDestinationExchangesForCurrency(currentCurrency)) {
						final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getTransferExchangeActionNode(destinationExchange, currentCurrency);
						paths.addAll( buildPaths( pathBuilder.addNode(exActionNode) ) );				
					}
				}
			case Fiat:
				default:
					if (pathBuilder.checkIfEndOfPathReachedWithNode() && pathBuilder.getSize() > 2) {
						paths.add(pathBuilder.build(STARTING_PATH_QUANTITY));
					} 
					for (InnerExchange innerEx : ex.getInnerExchangesForConvertingWith(currentCurrency)) {
						final ExchangeActionNode exActionNode = ExchangeActionNodeFactory.getBuySellExchangeActionNode(ex, innerEx);
						final PathBuilder newPathBuilder = pathBuilder.addNodeIgnoreDuplicatesAndRespectDepth(exActionNode);
						if (newPathBuilder != null) {
							paths.addAll( buildPaths(newPathBuilder) );
						}				
					}
					break;					
			}
		}
		return paths;
	}
	
	public static void main(String[] args) {
		List<Path> paths = buildPaths(null);
		for (Path path : paths) {
			System.out.println(path + "\n");
		}
	}
	
}
