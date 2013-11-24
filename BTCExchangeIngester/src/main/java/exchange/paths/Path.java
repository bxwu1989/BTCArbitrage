package exchange.paths;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Ordering;

import exchange.client.Exchange;
import exchange.currency.CurrencyQuantDelegate;
import exchange.currency.CurrencyType;

public class Path {

	private final ImmutableList<ExchangeActionNode> actionNodes;
	private final CurrencyQuantDelegate originalQuantity;
	private CurrencyQuantDelegate finalQuantity;
	private CurrencyQuantDelegate percentDiff;
	private String pathString = "";
	
	public Path(final ImmutableList<ExchangeActionNode> actionNodes, final CurrencyQuantDelegate orginalQuantity) { 
		this.originalQuantity = orginalQuantity;
		this.actionNodes = actionNodes;		
	}
	
	public void updateFinalQuantity() {
		CurrencyQuantDelegate transitionalQuantity = originalQuantity;
		final StringBuilder sb = new StringBuilder();
		sb.append(transitionalQuantity.toString() + actionNodes.get(0).getSourceCurrency()  + "@" + actionNodes.get(0).getExchange()); 
		for (final ExchangeActionNode actionNode : actionNodes) { 
			transitionalQuantity = actionNode.getConvertedQuantity(transitionalQuantity);			
			sb.append(" -> " + transitionalQuantity.toString() + actionNode.getDestinationCurrency() + "@" + actionNode.getExchange());
		}
		
		setFinalQuantity(transitionalQuantity);
		updatePercentDiff();
		
		pathString = percentDiff.toString() + "% : " + sb.toString();
		System.out.println(pathString);
	}
	
	private void updatePercentDiff() {
		percentDiff = finalQuantity.subtract(originalQuantity).divide(originalQuantity).multiply(CurrencyQuantDelegate.getCurrencyQuant(100.00));
	}

	public CurrencyQuantDelegate getFinalQuantity() {
		return finalQuantity;
	}

	private void setFinalQuantity(CurrencyQuantDelegate finalQuantity) {
		this.finalQuantity = finalQuantity;
	}
	
	public CurrencyQuantDelegate getPercentDifference() {
		return originalQuantity.subtract(finalQuantity).divide(originalQuantity);
	}
	
	public static PathBuilder builder(int maxDepth, ExchangeActionNode initialExActionNode) {
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
	
	Ordering<Path> pathOrderByPercentDiffDesc = Ordering.natural().onResultOf(new Function<Path, CurrencyQuantDelegate>() {
		@Override public CurrencyQuantDelegate apply(Path input) {
			return input.getPercentDifference();
		}		
	}).reverse();
	
	static class PathBuilder {
		private final ImmutableList<ExchangeActionNode> exActionNodes;
		private final Set<Exchange> exchangesInPath = new HashSet<Exchange>();
		private final int depth;
		private final int maxDepth;
		
		private PathBuilder(int maxDepth, ExchangeActionNode initialExActionNode) {
			this.exActionNodes = ImmutableList.<ExchangeActionNode>builder().add(initialExActionNode).build();
			this.maxDepth = maxDepth;
			this.depth = 0;
			this.exchangesInPath.add(initialExActionNode.getExchange());
		}
		
		private PathBuilder(ImmutableList<ExchangeActionNode> exActionNodes,
				Set<Exchange> exchangesInPath,
				ExchangeActionNode latestExActionNode, int maxDepth, int depth) {
			final Builder<ExchangeActionNode> listBuilder = ImmutableList.builder();
			for (final ExchangeActionNode exActionNode : exActionNodes) { 
				listBuilder.add(exActionNode);
			}
			listBuilder.add(latestExActionNode);
			this.exActionNodes = listBuilder.build();
			this.exchangesInPath.addAll(exchangesInPath);
			this.exchangesInPath.add(latestExActionNode.getExchange());	
			this.maxDepth = maxDepth;
			this.depth = depth+1;
		}
		
		private PathBuilder copy(ExchangeActionNode latestExActionNode) {
			return new PathBuilder(exActionNodes, exchangesInPath, latestExActionNode, maxDepth, depth);
		};
		
		public boolean checkIfEndOfPathReachedWithNode() {
			return (getLastExchangeActionNode().getDestinationCurrency().equals(getFirstExchangeActionNode().getSourceCurrency()));
		}
		
		public int getSize() {
			return exActionNodes.size();
		}
		
		public PathBuilder addNode(ExchangeActionNode actionNode) {
			return copy(actionNode);
		}
		
		public PathBuilder addNodeIgnoreDuplicatesAndRespectDepth(ExchangeActionNode actionNode) {
			return ( depth >= maxDepth
					|| (actionNode.getSourceCurrency().getType().equals(CurrencyType.Fiat) 
							&& exActionNodes.contains(actionNode))
					|| (!actionNode.getSourceCurrency().getType().equals(CurrencyType.Fiat) 
							&& actionNode.getDestinationCurrency().equals(getLastExchangeActionNode().getSourceCurrency()) 
							&& actionNode.getSourceCurrency().equals(getLastExchangeActionNode().getDestinationCurrency()) ) ) ? 
							null : addNode(actionNode);
		}
		
		public Path build(final CurrencyQuantDelegate orginalQuantity) {
			final Path path = new Path(exActionNodes, orginalQuantity);
			registerPathWithExchanges(path);
			return path;
		}
		
		private void registerPathWithExchanges(Path path) {
			for (final Exchange ex : exchangesInPath) {
				ex.registerPathForUpdate(path);
			}
		}
		
		public ExchangeActionNode getLastExchangeActionNode() {
			return exActionNodes.get(exActionNodes.size()-1);
		}

		public ExchangeActionNode getFirstExchangeActionNode() {
			return exActionNodes.get(0);
		}
		
		public int getDepth() {
			return depth;
		}			
	}
}