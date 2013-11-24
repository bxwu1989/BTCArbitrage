package exchange.services.response;

import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableSortedSet;

import exchange.currency.CurrencyQuantDelegate;

/**
 * For Bids and Asks there is a list of Price and Quantity.
 */
public class MarketDepth {
    private final SortedSet<PriceQuantity> bids;
    private final SortedSet<PriceQuantity> asks;
    
    public MarketDepth(SortedSet<PriceQuantity> bids, SortedSet<PriceQuantity> asks) {
    	this.bids = bids;
        this.asks = asks;
    }

    /**
     * WARNING: Assumes that there are enough bids to be able to consume {@code quantity} worth.
     * 
     * @param quantity Quantity to sell.
     * @return
     */
    public CurrencyQuantDelegate consumeBids(CurrencyQuantDelegate quantity) {
    	CurrencyQuantDelegate newQuant = CurrencyQuantDelegate.getCurrencyQuant(0);
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
    public CurrencyQuantDelegate consumeAsks(CurrencyQuantDelegate quantity) {
    	CurrencyQuantDelegate newQuant = CurrencyQuantDelegate.getCurrencyQuant(0);
    	for (final PriceQuantity priceQuantity : asks) { 
    		final CurrencyQuantDelegate quantWanted = quantity.divide(priceQuantity.getPrice());
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Depth [bids=").append(bids).append("\nasks=")
				.append(asks).append("]");
		return builder.toString();
	}

	public static MarketDepthBuilder builder() {
		return new MarketDepthBuilder();
	}
	
	public static class MarketDepthBuilder {
		private final ImmutableSortedSet.Builder<PriceQuantity> askBuilder = ImmutableSortedSet.naturalOrder();
		private final ImmutableSortedSet.Builder<PriceQuantity> bidBuilder = ImmutableSortedSet.reverseOrder();
		
		private MarketDepthBuilder() {}
		
		public void addAsk(double price, double quantity) {
			addAsk(CurrencyQuantDelegate.getCurrencyQuant(price), CurrencyQuantDelegate.getCurrencyQuant(quantity));
		}
		public void addAsk(CurrencyQuantDelegate price, CurrencyQuantDelegate quantity) {
			askBuilder.add(new PriceQuantity(price, quantity));
		}
		
		public void addBid(double price, double quantity) {
			addBid(CurrencyQuantDelegate.getCurrencyQuant(price), CurrencyQuantDelegate.getCurrencyQuant(quantity));
		}
		public void addBid(CurrencyQuantDelegate price, CurrencyQuantDelegate quantity) {
			bidBuilder.add(new PriceQuantity(price, quantity));
		}
		
		public MarketDepth build() {
			return new MarketDepth(bidBuilder.build(), askBuilder.build());
		}
	}
	
	
	/**
	 * Price and Quantity Tuple.
	 */
	public static class PriceQuantity implements Comparable<PriceQuantity> {
	    private final CurrencyQuantDelegate price;
	    private final CurrencyQuantDelegate quantity;

	    private PriceQuantity(CurrencyQuantDelegate price, CurrencyQuantDelegate quantity) {
	        this.price = price;
	        this.quantity = quantity;
	    }

		public CurrencyQuantDelegate getPrice() {
			return price;
		}

		public CurrencyQuantDelegate getQuantity() {
			return quantity;
		}
		
		public CurrencyQuantDelegate getVolume() {
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
