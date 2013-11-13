package btc.exchange.client.requesthandlers;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import btc.exchange.client.BtcExchangeConfig;
import btc.exchange.client.framework.ExchangeApiScheduledService;
import btc.exchange.client.response.Depth;
import btc.exchange.client.response.Depth.PriceQuantity;
import btc.exchange.client.response.Ticker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Exchange(BtcExchangeConfig.CAMPBX)
public final class CampBXExchangeClient {	
	
	public static final class CampBXDepthScheduledServiceRequest extends ExchangeApiScheduledService {	
		private HttpGet depthHttpGet;	

		@Override
		public HttpGet getHttpRequest() {
			if (depthHttpGet ==null) {
				try {
					depthHttpGet = new HttpGet( new URIBuilder(EXCHANGE_CONFIG.getBaseURI())
					.setPath(EXCHANGE_CONFIG.getBaseURI().getPath() + "xdepth.php").build() );
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			return depthHttpGet;
		}

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final List<PriceQuantity> bids = jsonArrayToList(jsonObject.get("Bids").getAsJsonArray());
			final List<PriceQuantity> asks = jsonArrayToList(jsonObject.get("Asks").getAsJsonArray());
			final Depth depth = new Depth(bids, asks);

			EXCHANGE_CONFIG.updateDepth(depth);
		}
		
		private List<PriceQuantity> jsonArrayToList(final JsonArray jsonArray) {
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
	
	public static final class CampBXTickerScheduledServiceRequest extends ExchangeApiScheduledService {	
		private static HttpGet tickerHttpGet;	
		
		@Override
		public HttpGet getHttpRequest() {
			if (tickerHttpGet ==null) {
				try {
					tickerHttpGet = new HttpGet( new URIBuilder(EXCHANGE_CONFIG.getBaseURI())
					.setPath(EXCHANGE_CONFIG.getBaseURI().getPath() + "xticker.php").build() );
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
			return tickerHttpGet;
		}

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
	        final double lastTrade = jsonObject.get("Last Trade").getAsDouble();
	        final double bestBid = jsonObject.get("Best Bid").getAsDouble();
	        final double bestAsk = jsonObject.get("Best Ask").getAsDouble();
	        EXCHANGE_CONFIG.updateTicker(new Ticker(lastTrade, bestBid, bestAsk));
		}

		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(0, 500, TimeUnit.MILLISECONDS);
		}		
	}
	
}
