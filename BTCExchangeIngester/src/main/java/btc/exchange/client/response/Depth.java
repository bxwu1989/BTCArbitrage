package btc.exchange.client.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * For Bids and Asks there is a list of Price and Quantity.
 */
public class Depth {
    private List<PriceQuantity> bids;
    private List<PriceQuantity> asks;
    
    public Depth() {
    	this.bids = new ArrayList<>();
    	this.asks = new ArrayList<>();
    }
    public Depth(List<PriceQuantity> bids, List<PriceQuantity> asks) {
        this.bids = bids;
        this.asks = asks;
    }
    
    public void updateDepth(Depth depth) {
    	this.bids = depth.bids;
    	this.asks = depth.asks;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Depth [bids=").append(bids).append("\nasks=")
				.append(asks).append("]");
		return builder.toString();
	}

	/**
	 * Price and Quantity Tuple.
	 */
	public static class PriceQuantity {
	    private final double price;
	    private final double quantity;

	    public PriceQuantity(double price, double quantity) {
	        this.price = price;
	        this.quantity = quantity;
	    }

		public double getPrice() {
			return price;
		}

		public double getQuantity() {
			return quantity;
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
	}
	
}
