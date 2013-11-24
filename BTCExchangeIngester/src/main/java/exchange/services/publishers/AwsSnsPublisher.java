package exchange.services.publishers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.google.common.util.concurrent.AbstractScheduledService;

import exchange.Exchange;
import exchange.services.AWSClients;

public abstract class AwsSnsPublisher extends AbstractScheduledService {

	public abstract String getMessageBody(Exchange exConfig);
	public abstract Pattern getTopicArnPattern();
	
	@Override
	protected void runOneIteration() throws Exception {
		for (final Topic topic : AWSClients.getSNSClient().listTopics().getTopics()) {
			final Matcher arbitragePatternMatcher = getTopicArnPattern().matcher(topic.getTopicArn());
			if (arbitragePatternMatcher.matches()) {
				final Exchange exConfig = Exchange.fromString(arbitragePatternMatcher.group(1).toUpperCase());
				if (exConfig != null) {
					final PublishRequest request = new PublishRequest(topic.getTopicArn(), getMessageBody(exConfig));
					AWSClients.getSNSClient().publish(request);
				}
			}
		}
	}
}
