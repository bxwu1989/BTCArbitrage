package btc.exchange.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;

public enum BtcExchangeConfig {
	CAMPBX("campbx.com", "/api/", .006);
	
	private URI baseApiURI;	
	private double trxFee;
	private ExchangeClient exClient = null;
	
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
	
	private static final Set<ExchangeClient> exchangeClients = new HashSet<>();
	void registerExchangeClient(ExchangeClient exClient) {
/*		if (exClient != null) {
			throw new IllegalArgumentException("A client is already configured for " + this.toString() 
					+ ". There should only be a single client per exchange endpoint.");
		} else {*/
			this.exClient = exClient;
			exchangeClients.add(exClient);
//		}
	}
	
	public ExchangeClient getExClient() {
		return exClient;
	}

	public static Set<ExchangeClient> getAllExchangeClients() {
		return exchangeClients;
	};
	
}
