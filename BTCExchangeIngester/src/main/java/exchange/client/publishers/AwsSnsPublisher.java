package exchange.client.publishers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.google.common.util.concurrent.AbstractScheduledService;

import exchange.client.AWSClients;
import exchange.client.ExchangeConfig;

public abstract class AwsSnsPublisher extends AbstractScheduledService {

	public abstract String getMessageBody(ExchangeConfig exConfig);
	public abstract Pattern getTopicArnPattern();
	
	@Override
	protected void runOneIteration() throws Exception {
		for (final Topic topic : AWSClients.getSNSClient().listTopics().getTopics()) {
			final Matcher arbitragePatternMatcher = getTopicArnPattern().matcher(topic.getTopicArn());
			if (arbitragePatternMatcher.matches()) {
				final ExchangeConfig exConfig = ExchangeConfig.valueOf(arbitragePatternMatcher.group(1).toUpperCase());
				final PublishRequest request = new PublishRequest(topic.getTopicArn(), getMessageBody(exConfig));
				AWSClients.getSNSClient().publish(request);
			}
		}
	}
}
