package exchange.paths;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import exchange.Exchange;
import exchange.InnerExchange;
import exchange.currency.CurrencyType;
import exchange.currency.QuantityDelegate;

public class Path implements Comparable<Path> {

	private final ImmutableList<ExchangeAction> actionNodes;
	private final QuantityDelegate originalQuantity;
	private QuantityDelegate finalQuantity;
	private QuantityDelegate percentDiff;
	private String pathString = "";
	
	public Path(final ImmutableList<ExchangeAction> actionNodes, final QuantityDelegate orginalQuantity) { 
		this.originalQuantity = QuantityDelegate.getCurrencyQuant(orginalQuantity, actionNodes.get(0).getSourceCurrency());
		this.percentDiff = QuantityDelegate.getCurrencyQuant(0, actionNodes.get(0).getSourceCurrency());
		this.actionNodes = actionNodes;		
	}
	
	public void updateFinalQuantity() {
		QuantityDelegate transitionalQuantity = originalQuantity;
		final StringBuilder sb = new StringBuilder();
		sb.append(transitionalQuantity.toString() + actionNodes.get(0).getSourceCurrency()  + "@" + actionNodes.get(0).getExchange()); 
		for (final ExchangeAction actionNode : actionNodes) { 
			transitionalQuantity = actionNode.getConvertedQuantity(transitionalQuantity);			
			sb.append(" -> " + transitionalQuantity.toString() + actionNode.getDestinationCurrency() + "@" + actionNode.getExchange());
		}
		
		setFinalQuantity(transitionalQuantity);
		updatePercentDiff();
		
		pathString = percentDiff.toString() + "% : " + sb.toString();
		System.out.println(pathString);
	}
	
	private void updatePercentDiff() {
		percentDiff = finalQuantity.subtract(originalQuantity).divide(originalQuantity).multiply(QuantityDelegate.getQuant(100.00));
	}

	public QuantityDelegate getFinalQuantity() {
		return finalQuantity;
	}

	private void setFinalQuantity(QuantityDelegate finalQuantity) {
		this.finalQuantity = finalQuantity;
	}
	
	public QuantityDelegate getPercentDifference() {
		return originalQuantity.subtract(finalQuantity).divide(originalQuantity);
	}
	
	public static PathBuilder builder(int maxDepth, ExchangeAction initialExActionNode) {
		return new PathBuilder(maxDepth, initialExActionNode);
	}
	
	@Override
	public String toString() {
		return pathString;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
	public int compareTo(Path o) {
		return percentDiff.compareTo(o.percentDiff);
	}
	
	static class PathBuilder {
		private final ImmutableList<ExchangeAction> exActionNodes;
		private final Map<Exchange, Set<InnerExchange>> innerExchangesInPath = new EnumMap<>(Exchange.class);
		private final int depth;
		private final int maxDepth;
		
		private PathBuilder(int maxDepth, ExchangeAction initialExActionNode) {
			this.exActionNodes = ImmutableList.<ExchangeAction>builder().add(initialExActionNode).build();
			this.maxDepth = maxDepth;
			this.depth = 0;
			
			final Set<InnerExchange> innerExs = new HashSet<>();
			innerExs.add(InnerExchange.getInnerExchange(initialExActionNode.getSourceCurrency(), initialExActionNode.getDestinationCurrency()));
			innerExchangesInPath.put(initialExActionNode.getExchange(), innerExs);
		}
		
		private PathBuilder(ImmutableList<ExchangeAction> exActionNodes, Map<Exchange, Set<InnerExchange>> innerExchangesInPath,
				ExchangeAction latestExActionNode, int maxDepth, int depth) {
			final Builder<ExchangeAction> listBuilder = ImmutableList.builder();
			for (final ExchangeAction exActionNode : exActionNodes) { 
				listBuilder.add(exActionNode);
			}
			listBuilder.add(latestExActionNode);
			this.exActionNodes = listBuilder.build();
			for (Entry<Exchange, Set<InnerExchange>> entry : innerExchangesInPath.entrySet()) {
				for (InnerExchange innerEx : entry.getValue()) {
					updateInnerExchangesPerExchange(entry.getKey(), innerEx);
				}
			}
			updateInnerExchangesPerExchange(latestExActionNode);
			this.maxDepth = maxDepth;
			this.depth = depth+1;
		}
		
		private void updateInnerExchangesPerExchange(ExchangeAction exchangeActionNode) {
			updateInnerExchangesPerExchange(exchangeActionNode.getExchange(), 
					InnerExchange.getInnerExchange(exchangeActionNode.getSourceCurrency(), exchangeActionNode.getDestinationCurrency()));
		}
		private void updateInnerExchangesPerExchange(Exchange ex, InnerExchange innerEx) {
			if (innerEx != null) {
				Set<InnerExchange> innerExs = innerExchangesInPath.get(ex);
				if (innerExs == null) {
					innerExs = new HashSet<>();
					innerExchangesInPath.put(ex, innerExs);
				}
				innerExs.add(innerEx);
			}
		}
		
		private PathBuilder copy(ExchangeAction latestExActionNode) {
			return new PathBuilder(exActionNodes, innerExchangesInPath, latestExActionNode, maxDepth, depth);
		};
		
		public boolean checkIfEndOfPathReachedWithNode() {
			return (getLastExchangeActionNode().getDestinationCurrency().equals(getFirstExchangeActionNode().getSourceCurrency()));
		}
		
		public int getSize() {
			return exActionNodes.size();
		}
		
		public PathBuilder addNode(ExchangeAction actionNode) {
			return copy(actionNode);
		}
		
		public PathBuilder addNodeIgnoreDuplicatesAndRespectDepth(ExchangeAction actionNode) {
			return ( depth >= maxDepth
					|| (actionNode.getSourceCurrency().getType().equals(CurrencyType.Fiat) // Allow duplicate Digital Currency transfers and sells,
							&& exActionNodes.contains(actionNode)) 							// but block duplicate buys from the same exchange.
					|| (actionNode.getSourceCurrency().getType().equals(CurrencyType.Digital) // Block a buy and then sell in the same exchange with the same currency
							&& actionNode.getDestinationCurrency().equals(getLastExchangeActionNode().getSourceCurrency()) 
							&& actionNode.getSourceCurrency().equals(getLastExchangeActionNode().getDestinationCurrency()) ) ) ? 
							null : addNode(actionNode);
		}
		
		public Path buildAndRegisterWithExchangesInnerExchanges(final QuantityDelegate orginalQuantity) {
			final Path path = new Path(exActionNodes, orginalQuantity);
			Exchange.registerPathsWithExchangesInnerExchanges(path, innerExchangesInPath);
			return path;
		}
		
		public ExchangeAction getLastExchangeActionNode() {
			return exActionNodes.get(exActionNodes.size()-1);
		}

		public ExchangeAction getFirstExchangeActionNode() {
			return exActionNodes.get(0);
		}
		
		public int getDepth() {
			return depth;
		}			
	}
}