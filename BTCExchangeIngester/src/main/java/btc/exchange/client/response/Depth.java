package btc.exchange.client.response;

import java.util.ArrayList;
import java.util.List;

/**
 * For Bids and Asks there is a list of Price and Quantity.
 */
public class Depth implements BtcExchangeResponse {
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
		builder.append("Depth [bids=").append(bids).append(", asks=")
				.append(asks).append("]");
		return builder.toString();
	}

}
