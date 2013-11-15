package btc.exchange.client.response;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Ticker {
    private double lastTrade;
    private double bestAsk;
    private double bestBid;

    private final DescriptiveStatistics runningTradeHistory = new DescriptiveStatistics(60);
    
    public Ticker() { 	
    }

	public Ticker(double lastTrade, double bestAsk, double bestBid) {
        this.lastTrade = lastTrade;
        this.bestAsk = bestAsk;
        this.bestBid = bestBid;
    }

	public void updateTicker(Ticker ticker) {
		lastTrade = ticker.lastTrade;
		runningTradeHistory.addValue(lastTrade);
        bestAsk = ticker.bestAsk;
        bestBid = ticker.bestBid;
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

    public double getRunningTradeMean() {
    	return runningTradeHistory.getMean();
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
