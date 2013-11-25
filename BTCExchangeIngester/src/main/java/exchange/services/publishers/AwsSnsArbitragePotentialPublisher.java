package exchange.services.publishers;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exchange.Exchange;

public class AwsSnsArbitragePotentialPublisher extends AwsSnsPublisher {

	private static final Pattern ARBITRAGE_TOPIC_PATTERN = Pattern.compile(
			".*:(\\w*)_Arbitrage_Data$", Pattern.CASE_INSENSITIVE);

	@Override
	public Pattern getTopicArnPattern() {
		return ARBITRAGE_TOPIC_PATTERN;
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.DAYS); // TODO pull from config;
	}

	@Override
	public String getMessageBody(Matcher matcher) {
		final Exchange exConfig = Exchange.fromString(matcher.group(1).toUpperCase());
		if (exConfig != null) {
			// get any exchange specific data you want to report and build a message string
		}
		return "test";
	}

}
