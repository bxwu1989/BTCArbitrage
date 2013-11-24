package exchange.configuration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;

import exchange.Exchange;

public class ExchangeApiConfig {

	
	private static final Map<Exchange, String> API_CONNECTION_STRINGS;
	private static final Map<Exchange, Scheduler> API_TIMEOUT_SCHEDULERS;
	static {
		final ImmutableMap.Builder<Exchange, String> builder = ImmutableMap.builder();
		builder.put(Exchange.CAMPBX, "http://campbx.com/api/");
		builder.put(Exchange.MTGOX, "http://data.mtgox.com/api/2/");
		API_CONNECTION_STRINGS = builder.build();
		
		final ImmutableMap.Builder<Exchange, Scheduler> timeoutBuilder = ImmutableMap.builder();
		timeoutBuilder.put(Exchange.CAMPBX, Scheduler.newFixedRateSchedule(0, 500, TimeUnit.MILLISECONDS));
		
		API_TIMEOUT_SCHEDULERS = timeoutBuilder.build();
	}
	
	public static String getApiString(Exchange ex) {
		return API_CONNECTION_STRINGS.get(ex);
	}
	
	public static Scheduler getDefaultApiTimeoutScheduler(Exchange ex) {
		return API_TIMEOUT_SCHEDULERS.get(ex);
	}
	
	private ExchangeApiConfig() {}
}
