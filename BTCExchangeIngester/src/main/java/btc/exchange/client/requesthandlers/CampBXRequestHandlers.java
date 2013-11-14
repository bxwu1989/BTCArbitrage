package btc.exchange.client.requesthandlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import btc.exchange.client.ExchangeConfig;
import btc.exchange.client.response.Depth;
import btc.exchange.client.response.Depth.PriceQuantity;
import btc.exchange.client.response.Ticker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Exchange(ExchangeConfig.CAMPBX)
public final class CampBXRequestHandlers {	
	
	public static final class CampBXDepthScheduledServiceRequest extends ExchangeApiRequestHandler {

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final List<PriceQuantity> bids = jsonArrayToList(jsonObject.get("Bids").getAsJsonArray());
			final List<PriceQuantity> asks = jsonArrayToList(jsonObject.get("Asks").getAsJsonArray());
			final Depth depth = new Depth(bids, asks);

			EXCHANGE_CONFIG.updateDepth(depth);
		}

		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(0, 30000, TimeUnit.MILLISECONDS); // TODO pull from config
		}

		@Override
		protected String getApiMethodPath() {
			return "xdepth.php"; // TODO pull from config
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
	}
	
	public static final class CampBXTickerScheduledServiceRequest extends ExchangeApiRequestHandler {

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
			return Scheduler.newFixedRateSchedule(0, 1000, TimeUnit.MILLISECONDS); // TODO pull from config
		}

		@Override
		protected String getApiMethodPath() {
			return "xticker.php"; // TODO pull from config
		}		
	}
	
}
