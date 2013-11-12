package btc.exchange.client;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import btc.exchange.client.framework.ExchangeApiScheduledService;
import btc.exchange.client.response.Depth;
import btc.exchange.client.response.PriceQuantity;
import btc.exchange.client.response.Ticker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class CampBXExchangeClient extends ExchangeClient {	
	private static final BtcExchangeConfig CAMPBX_EXCHANGE_CONFIG = BtcExchangeConfig.CAMPBX;

	public static void init() {
		CAMPBX_EXCHANGE_CONFIG.registerExchangeClient(new CampBXExchangeClient());
	}
	
	private CampBXExchangeClient() {
	}

	
	@Override
	public List<ExchangeApiScheduledService> getBtcExchangeMarketRequests() {
		return marketRequests;
	}
	
	private static final List<ExchangeApiScheduledService> marketRequests = new ArrayList<ExchangeApiScheduledService>();
	static {
		marketRequests.add(new CampBXDepthScheduledServiceRequest());
		marketRequests.add(new CampBXTickerScheduledServiceRequest());
	}
	
	private static class CampBXDepthScheduledServiceRequest extends ExchangeApiScheduledService {	
		private static HttpGet depthHttpGet;	
		static {
			try {
				depthHttpGet = new HttpGet( new URIBuilder(CAMPBX_EXCHANGE_CONFIG.getBaseURI())
				.setPath(CAMPBX_EXCHANGE_CONFIG.getBaseURI().getPath() + "xdepth.php").build() );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public HttpGet getHttpGet() {
			return depthHttpGet;
		}

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final List<PriceQuantity> bids = decodeArray(jsonObject.get("Bids").getAsJsonArray());
			final List<PriceQuantity> asks = decodeArray(jsonObject.get("Asks").getAsJsonArray());
			CAMPBX_EXCHANGE_CONFIG.getExClient().updateDepth(new Depth(bids, asks));
			System.out.println("Updated client depth.\n" + CAMPBX_EXCHANGE_CONFIG);
		}
		
		private List<PriceQuantity> decodeArray(final JsonArray jsonArray) {
			final List<PriceQuantity> pqList = new ArrayList<>();
	        final Iterator<JsonElement> iter = jsonArray.iterator();
	        while (iter.hasNext()) {
	        	final JsonArray encodedPriceQuantity = (JsonArray)iter.next();
	            final double price = encodedPriceQuantity.get(0).getAsDouble();
	        	final double quantity = encodedPriceQuantity.get(1).getAsDouble();
	            pqList.add(new PriceQuantity(price, quantity));
	        }	        
	        return pqList;
	    }	

		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(0, 30000, TimeUnit.MILLISECONDS);
		}
		
	}
	
	private static class CampBXTickerScheduledServiceRequest extends ExchangeApiScheduledService {	
		private static HttpGet depthHttpGet;	
		static {
			try {
				depthHttpGet = new HttpGet( new URIBuilder(CAMPBX_EXCHANGE_CONFIG.getBaseURI())
				.setPath(CAMPBX_EXCHANGE_CONFIG.getBaseURI().getPath() + "xticker.php").build() );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public HttpGet getHttpGet() {
			return depthHttpGet;
		}

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
	        final double lastTrade = jsonObject.get("Last Trade").getAsDouble();
	        final double bestBid = jsonObject.get("Best Bid").getAsDouble();
	        final double bestAsk = jsonObject.get("Best Ask").getAsDouble();
	        CAMPBX_EXCHANGE_CONFIG.getExClient().updateTicker(new Ticker(lastTrade, bestBid, bestAsk));
	        System.out.println("Updated client ticker.\n" + CAMPBX_EXCHANGE_CONFIG);
		}

		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(0, 500, TimeUnit.MILLISECONDS);
		}		
	}
	
}
