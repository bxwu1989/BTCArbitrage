package exchange.services.response;

import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableSortedSet;

import exchange.currency.Currency;
import exchange.currency.QuantityDelegate;

/**
 * For Bids and Asks there is a list of Price and Quantity.
 */
public class MarketDepth {
    private final SortedSet<PriceQuantity> bids;
    private final SortedSet<PriceQuantity> asks;
    private final Currency source;
    private final Currency destination;
    
    public MarketDepth(SortedSet<PriceQuantity> bids, SortedSet<PriceQuantity> asks, Currency source, Currency destination) {
    	this.bids = bids;
        this.asks = asks;
        this.source = source;
        this.destination = destination;
    }

    /**
     * WARNING: Assumes that there are enough bids to be able to consume {@code quantity} worth.
     * 
     * @param quantity Quantity to sell.
     * @return
     */
    public QuantityDelegate consumeBids(QuantityDelegate quantity) {
    	QuantityDelegate newQuant = QuantityDelegate.getCurrencyQuant(0, source);
    	for (final PriceQuantity priceQuantity : bids) { 
    		if (quantity.compareTo(priceQuantity.getQuantity()) > 0) {  // want more than available, consume entire bid.
    			quantity = quantity.subtract(priceQuantity.getQuantity());
    			newQuant = newQuant.add(priceQuantity.getVolume());
    		} else { // consume just the amount needed and break out
    			newQuant = newQuant.add(quantity.multiply(priceQuantity.getPrice()));
    			break;
    		}
    	}
    	return newQuant;
    }
    
    /**
     * WARNING: Assumes that there are enough asks to be able to consume {@code quantity} worth.
     * 
     * @param quantity Quantity to buy.
     * @return
     */
    public QuantityDelegate consumeAsks(QuantityDelegate quantity) {
    	QuantityDelegate newQuant = QuantityDelegate.getCurrencyQuant(0, destination);
    	for (final PriceQuantity priceQuantity : asks) { 
    		final QuantityDelegate quantWanted = quantity.divide(priceQuantity.getPrice());
    		if (quantWanted.compareTo(priceQuantity.getQuantity()) > 0) { // want more than available, consume entire ask.
    			quantity = quantity.subtract(priceQuantity.getVolume());  // update how much we still need
    			newQuant = newQuant.add(priceQuantity.getQuantity());
    		} else { // consume just the amount needed and break out
    			newQuant = newQuant.add(quantWanted);
    			break;
    		}
    	}   	
    	return newQuant;
    }

	public Currency getSource() {
		return source;
	}

	public Currency getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Depth [bids=").append(bids).append("\nasks=")
				.append(asks).append("]");
		return builder.toString();
	}

	public static MarketDepthBuilder builder(Currency source, Currency destination) {
		return new MarketDepthBuilder(source, destination);
	}
	
	public static class MarketDepthBuilder {
		private final ImmutableSortedSet.Builder<PriceQuantity> askBuilder = ImmutableSortedSet.naturalOrder(); // The ordering here is very important!
		private final ImmutableSortedSet.Builder<PriceQuantity> bidBuilder = ImmutableSortedSet.reverseOrder(); // We want to consume the lowest asks
		private final Currency source;																			// and the highest bids.
	    private final Currency destination;
	    
		private MarketDepthBuilder(Currency source, Currency destination) {
	        this.source = source;
	        this.destination = destination;
	    }
		
		public void addAsk(double price, double quantity) {
			add(price, quantity, askBuilder);
		}

		public void addBid(double price, double quantity) {
			add(price, quantity, bidBuilder);
		}
		
		private void add(double price, double quantity, ImmutableSortedSet.Builder<PriceQuantity> builder) {
			builder.add(new PriceQuantity(QuantityDelegate.getCurrencyQuant(price, source), QuantityDelegate.getCurrencyQuant(quantity, destination)));
		}
		
		public MarketDepth build() {
			return new MarketDepth(bidBuilder.build(), askBuilder.build(), source, destination);
		}
	}
	
	
	/**
	 * Price and Quantity Tuple.
	 */
	private static class PriceQuantity implements Comparable<PriceQuantity> {
	    private final QuantityDelegate price;
	    private final QuantityDelegate quantity;

	    private PriceQuantity(QuantityDelegate price, QuantityDelegate quantity) {
	        this.price = price;
	        this.quantity = quantity;
	    }

		public QuantityDelegate getPrice() {
			return price;
		}

		public QuantityDelegate getQuantity() {
			return quantity;
		}
		
		public QuantityDelegate getVolume() {
			return quantity.multiply(price);
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PriceQuantity [price=").append(price)
					.append(", quantity=").append(quantity).append("]");
			return builder.toString();
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
		public int compareTo(PriceQuantity o) {
			return price.compareTo(o.price);
		}
	}
	
}
