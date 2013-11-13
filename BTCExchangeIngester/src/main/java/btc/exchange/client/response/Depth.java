package btc.exchange.client.response;

import java.util.ArrayList;
import java.util.List;

/**
 * For Bids and Asks there is a list of Price and Quantity.
 */
public class Depth {
    private List<PriceQuantity> bids;
    private List<PriceQuantity> asks;

    public Depth() {
    	this.bids = new ArrayList<PriceQuantity>();
    	this.asks = new ArrayList<PriceQuantity>();
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
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(price);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(quantity);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PriceQuantity other = (PriceQuantity) obj;
			if (Double.doubleToLongBits(price) != Double
					.doubleToLongBits(other.price))
				return false;
			if (Double.doubleToLongBits(quantity) != Double
					.doubleToLongBits(other.quantity))
				return false;
			return true;
		}
	}
	
}
