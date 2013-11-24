package exchange.services.requesthandlers;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import exchange.Exchange;
import exchange.services.response.MarketDepth;
import exchange.services.response.MarketDepth.MarketDepthBuilder;
import exchange.services.response.Ticker;
import exchange.currency.Currency;

@ExchangeTag(Exchange.CAMPBX)
public final class CampBXRequestHandlers {	
	
	public static final class CampBXDepthScheduledServiceRequest extends ExchangeApiRequestHandler  {

		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final MarketDepth marketDepth = buildMarketDepthFromJson(jsonObject.get("Bids").getAsJsonArray(), jsonObject.get("Asks").getAsJsonArray());
			exchange.updateMarketDepth(Currency.USD, Currency.BTC, marketDepth);
		}

		@Override
		protected String getApiMethodPath() {
			return "xdepth.php"; // TODO pull from config
		}
		
		private MarketDepth buildMarketDepthFromJson(final JsonArray bids, final JsonArray asks) {
			final MarketDepthBuilder marketDepthBuilder = MarketDepth.builder();
			final Iterator<JsonElement> bidIter = bids.iterator();
	        while (bidIter.hasNext()) {
	        	final JsonArray encodedPriceQuantity = (JsonArray)bidIter.next();
	        	marketDepthBuilder.addBid(encodedPriceQuantity.get(0).getAsDouble(), encodedPriceQuantity.get(1).getAsDouble());
	        }
	        final Iterator<JsonElement> askIter = asks.iterator();
	        while (askIter.hasNext()) {
	        	final JsonArray encodedPriceQuantity = (JsonArray)askIter.next();
	        	marketDepthBuilder.addAsk(encodedPriceQuantity.get(0).getAsDouble(), encodedPriceQuantity.get(1).getAsDouble());
	        }	
	        
	        return marketDepthBuilder.build();
		}
	}
	
	public static final class CampBXTickerScheduledServiceRequest extends ExchangeApiRequestHandler {
		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
	        final double lastTrade = jsonObject.get("Last Trade").getAsDouble();
	        final double bestBid = jsonObject.get("Best Bid").getAsDouble();
	        final double bestAsk = jsonObject.get("Best Ask").getAsDouble();
	        exchange.updateTicker(new Ticker(lastTrade, bestBid, bestAsk));
		}

		@Override
		protected String getApiMethodPath() {
			return "xticker.php"; // TODO pull from config
		}		
	}
	
}
