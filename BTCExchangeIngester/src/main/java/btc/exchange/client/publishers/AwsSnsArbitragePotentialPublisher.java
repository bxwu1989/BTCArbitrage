package btc.exchange.client.publishers;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import btc.exchange.client.ExchangeConfig;

public class AwsSnsArbitragePotentialPublisher extends AwsSnsPublisher {

	@Override
	public String getMessageBody(ExchangeConfig exConfig) {
		return exConfig.getDepth().toString();
	}

	private static final Pattern ARBITRAGE_TOPIC_PATTERN = Pattern.compile(".*:(\\w*)_Arbitrage_Data$");
	@Override
	public Pattern getTopicArnPattern() {
		return ARBITRAGE_TOPIC_PATTERN;
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 60000, TimeUnit.MILLISECONDS); // TODO pull from config;
	}

}
