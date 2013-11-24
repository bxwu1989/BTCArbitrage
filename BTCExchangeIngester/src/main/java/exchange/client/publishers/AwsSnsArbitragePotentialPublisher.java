package exchange.client.publishers;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import exchange.client.Exchange;

public class AwsSnsArbitragePotentialPublisher extends AwsSnsPublisher {

	@Override
	public String getMessageBody(Exchange exConfig) {
		return "test";
	}

	private static final Pattern ARBITRAGE_TOPIC_PATTERN = Pattern.compile(
			".*:(\\w*)_Arbitrage_Data$", Pattern.CASE_INSENSITIVE);

	@Override
	public Pattern getTopicArnPattern() {
		return ARBITRAGE_TOPIC_PATTERN;
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 60000, TimeUnit.MILLISECONDS); // TODO pull from config;
	}

}
