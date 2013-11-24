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

@ExchangeTag(Exchange.BITFINIX)
public class BitfinixRequestHandlers {

	public static final class BitfinixMarketDepthScheduledServiceRequest extends ExchangeApiRequestHandler  {
		@Override
		public void handleResponse(final String jsonResponse) {
			final JsonObject jsonObject = new JsonParser().parse(jsonResponse).getAsJsonObject();
			final MarketDepth marketDepth = buildMarketDepthFromJson(jsonObject.get("bids").getAsJsonArray(), jsonObject.get("asks").getAsJsonArray());
			exchange.updateMarketDepth(Currency.USD, Currency.BTC, marketDepth);
		}

		@Override
		protected String getApiMethodPath() {
			return "book/btcusd"; 
		}
		
		private MarketDepth buildMarketDepthFromJson(final JsonArray bids, final JsonArray asks) {
			final MarketDepthBuilder marketDepthBuilder = MarketDepth.builder();
			final Iterator<JsonElement> bidIter = bids.iterator();
	        while (bidIter.hasNext()) {
	        	final JsonObject encodedPriceQuantity = (JsonObject) bidIter.next();
	        	marketDepthBuilder.addBid(encodedPriceQuantity.get("price").getAsDouble(), encodedPriceQuantity.get("amount").getAsDouble());
	        }
	        final Iterator<JsonElement> askIter = asks.iterator();
	        while (askIter.hasNext()) {
	        	final JsonObject encodedPriceQuantity = (JsonObject)askIter.next();
	        	marketDepthBuilder.addAsk(encodedPriceQuantity.get("price").getAsDouble(), encodedPriceQuantity.get("amount").getAsDouble());
	        }	
	        
	        return marketDepthBuilder.build();
		}
	}
}
