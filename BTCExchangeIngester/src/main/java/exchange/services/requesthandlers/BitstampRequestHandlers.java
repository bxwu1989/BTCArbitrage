package exchange.services.requesthandlers;

import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import exchange.Exchange;
import exchange.currency.Currency;
import exchange.services.response.MarketDepth;
import exchange.services.response.MarketDepth.MarketDepthBuilder;

@ExchangeTag(Exchange.BITSTAMP)
public class BitstampRequestHandlers {

	public static final class BitstampMarketDepthScheduledServiceRequest extends ExchangeApiRequestHandler  {

		@Override
		public void handleResponse(final String jsonResponse) {
		//	System.out.println(jsonResponse);
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final MarketDepth marketDepth = buildMarketDepthFromJson(jsonObject.get("bids").getAsJsonArray(), jsonObject.get("asks").getAsJsonArray());
			exchange.updateMarketDepth(Currency.USD, Currency.BTC, marketDepth);
		}

		@Override
		protected String getApiMethodPath() {
			return "order_book/"; 
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
}
