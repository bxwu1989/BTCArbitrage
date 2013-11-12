package btc.exchange.client.response;

/**
 * Price and Quantity Tuple.
 */
public class PriceQuantity {
    private final double price;
    private final double quantity;

    public PriceQuantity(double price, double quantity) {
        this.price = price;
        this.quantity = quantity;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PriceQuantity [price=").append(price)
				.append(", quantity=").append(quantity).append("]");
		return builder.toString();
	}

	public double getPrice() {
		return price;
	}

	public double getQuantity() {
		return quantity;
	}
}
