package exchange.configuration;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import exchange.client.Exchange;

public class ExchangeApiConfig {

	
	private static final Map<Exchange, String> API_CONNECTION_STRINGS;
	static {
		ImmutableMap.Builder<Exchange, String> builder = ImmutableMap.builder();
		builder.put(Exchange.CAMPBX, "http://campbx.com/api/");
		builder.put(Exchange.MTGOX, "http://data.mtgox.com/api/2/");
		API_CONNECTION_STRINGS = builder.build();
	}
	public static String getApiString(Exchange ex) {
		return API_CONNECTION_STRINGS.get(ex);
	}
	
	private ExchangeApiConfig() {}
}
