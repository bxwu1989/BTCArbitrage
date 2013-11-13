package btc.exchange.client.requesthandlers;

import java.util.concurrent.TimeUnit;

import btc.exchange.client.BtcExchangeConfig;
import btc.exchange.client.framework.ExchangeApiScheduledService;

@Exchange(BtcExchangeConfig.MTGOX)
public class MtGoxRequestHandlers {
	
	public static final class MtGoxTickerscheduledServiceRequest extends ExchangeApiScheduledService {

		@Override
		protected void handleResponse(String jsonResponse) {
			// {"result":"success","data":{
			// "last_local":{"value":"386.16210","value_int":"38616210","display":"$386.16","display_short":"$386.16","currency":"USD"},
			// "last":{"value":"386.16210","value_int":"38616210","display":"$386.16","display_short":"$386.16","currency":"USD"},
			// "last_orig":{"value":"386.16210","value_int":"38616210","display":"$386.16","display_short":"$386.16","currency":"USD"},
			// "last_all":{"value":"386.16210","value_int":"38616210","display":"$386.16","display_short":"$386.16","currency":"USD"},
			//"buy":{"value":"386.20071","value_int":"38620071","display":"$386.20","display_short":"$386.20","currency":"USD"},
			//"sell":{"value":"389.08500","value_int":"38908500","display":"$389.09","display_short":"$389.09","currency":"USD"},"now":"1384316690297786"}}
			System.out.println(jsonResponse);
		}
	
		@Override
		protected String getApiMethodPath() {
			return "BTCUSD/money/ticker_fast";
		}
	
		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(0, 1000, TimeUnit.MILLISECONDS); // TODO pull from config
		}	
	}
}
