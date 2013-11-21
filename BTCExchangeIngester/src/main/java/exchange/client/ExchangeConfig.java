package exchange.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import exchange.client.response.Depth;
import exchange.client.response.Ticker;

public enum ExchangeConfig {
	CAMPBX("campbx.com", "/api/", .006),
	MTGOX("data.mtgox.com", "/api/2/", .01);
	
	// Configuration
	private URI baseApiURI;	
	private double trxFee;
	
	// Active Exchange Data
	private final Depth depth = new Depth();
	private final Ticker ticker = new Ticker();
	
	private ExchangeConfig(final String apiHost, final String apiPath, double trxFee) {
		try {
			this.baseApiURI = new URIBuilder()
				.setScheme("http").setHost(apiHost).setPath(apiPath).build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}		
		
		this.trxFee = trxFee;
	}

	public URI getBaseURI() {
		return baseApiURI;
	}
	
	public void updateDepth(Depth depth) {
		this.depth.updateDepth(depth);
		System.out.println("Updated " + this + this.depth);
	}
	
	public void updateTicker(Ticker ticker) {
		this.ticker.updateTicker(ticker);
		System.out.println("Updated " + this + this.ticker);
	}
	
	public Depth getDepth() {
		return depth;
	}
	
}
