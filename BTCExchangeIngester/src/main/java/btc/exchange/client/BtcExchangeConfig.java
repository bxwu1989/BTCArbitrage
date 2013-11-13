package btc.exchange.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import btc.exchange.client.response.Depth;
import btc.exchange.client.response.Ticker;

public enum BtcExchangeConfig {
	CAMPBX("campbx.com", "/api/", .006);
	
	private URI baseApiURI;	
	private double trxFee;
	final Depth depth = new Depth();
	private final Ticker ticker = new Ticker();
	
	BtcExchangeConfig(final String apiHost, final String apiPath, double trxFee) {
		try {
			this.baseApiURI = new URIBuilder()
				.setScheme("http").setHost(apiHost).setPath(apiPath).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}		
		
		this.trxFee = trxFee;
	}

	public double getTrxFee() {
		return trxFee;
	}


	public URI getBaseURI() {
		return baseApiURI;
	}
	
	public void updateDepth(Depth depth) {
		this.depth.updateDepth(depth);
		System.out.println("Updated client depth.\n" + this + "\n" + this.depth);
	}
	
	public void updateTicker(Ticker ticker) {
		this.ticker.updateTicker(ticker);
		System.out.println("Updated client ticker.\n" + this);
        System.out.println(ticker);
	}
	
}
