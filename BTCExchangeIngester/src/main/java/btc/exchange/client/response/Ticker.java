package btc.exchange.client.response;


/**
 * The response back from /xticker.
 */
public class Ticker {
    private double lastTrade;
    private double bestAsk;
    private double bestBid;

    public Ticker() { 	
    }

	public Ticker(double lastTrade, double bestAsk, double bestBid) {
        this.lastTrade = lastTrade;
        this.bestAsk = bestAsk;
        this.bestBid = bestBid;
    }

	public void updateTicker(Ticker ticker) {
		this.lastTrade = ticker.lastTrade;
        this.bestAsk = ticker.bestAsk;
        this.bestBid = ticker.bestBid;
	}
	
    public double getLastTrade() {
        return this.lastTrade;
    }
    public double getLastAsk() {
        return this.bestAsk;
    }
    public double getBestBid() {
        return this.bestBid;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Ticker [lastTrade=").append(lastTrade)
				.append(", bestAsk=").append(bestAsk).append(", bestBid=")
				.append(bestBid).append("]");
		return builder.toString();
	}
}
