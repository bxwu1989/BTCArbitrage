package exchange.services.publishers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Ordering;

import exchange.Exchange;
import exchange.paths.Path;

public class AwsSnsArbitragePathsPublisher extends AwsSnsPublisher {

	private static final int NUM_PATHS_TO_REPORT = 10;
	
	private static final Pattern ARBITRAGE_PATHS_TOPIC_PATTERN = Pattern.compile(
			".*:(\\w*)_Arbitrage_Paths$", Pattern.CASE_INSENSITIVE);
	@Override
	public String getMessageBody(Matcher matcher) {
		final List<Path> topPaths = Ordering.natural().reverse().leastOf(Exchange.getAllPaths(), NUM_PATHS_TO_REPORT);
		final StringBuilder pathStringBuilder = new StringBuilder("Top"+ NUM_PATHS_TO_REPORT + " Paths:");
		for (int i = 0 ; i < topPaths.size(); i++) {
			pathStringBuilder.append("\n" + (i+1) + ". ").append(topPaths.get(i));
		}
		System.out.println(pathStringBuilder.toString());
		return pathStringBuilder.toString();
	}

	@Override
	public Pattern getTopicArnPattern() {
		return ARBITRAGE_PATHS_TOPIC_PATTERN;
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.HOURS); // TODO pull from config;
	}

}
