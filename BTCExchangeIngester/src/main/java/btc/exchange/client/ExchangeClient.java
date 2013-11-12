package btc.exchange.client;

import java.util.List;

import btc.exchange.client.framework.ExchangeApiScheduledService;
import btc.exchange.client.response.Depth;
import btc.exchange.client.response.Ticker;

public abstract class ExchangeClient {

	private final Depth depth = new Depth();
	private final Ticker ticker = new Ticker();
	
	abstract List<ExchangeApiScheduledService> getBtcExchangeMarketRequests();

	public Depth getDepth() {
		return depth;
	}

	protected void updateDepth(Depth depth) {
		this.depth.updateDepth(depth);
	}
	
	protected void updateTicker(Ticker ticker) {
		ticker.updateTicker(ticker);
	}

	public Ticker getTicker() {
		return ticker;
	}

}
