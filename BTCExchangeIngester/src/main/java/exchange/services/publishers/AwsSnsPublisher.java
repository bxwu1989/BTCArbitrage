package exchange.services.publishers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import com.google.common.util.concurrent.AbstractScheduledService;

import exchange.services.AWSClients;

public abstract class AwsSnsPublisher extends AbstractScheduledService {

	public abstract String getMessageBody(Matcher matcher);
	public abstract Pattern getTopicArnPattern();
	
	@Override
	protected void runOneIteration() throws Exception {
		for (final Topic topic : AWSClients.getSNSClient().listTopics().getTopics()) {
			final Matcher matcher = getTopicArnPattern().matcher(topic.getTopicArn());
			if (matcher.matches()) {
				final PublishRequest request = new PublishRequest(topic.getTopicArn(), getMessageBody(matcher));
				AWSClients.getSNSClient().publish(request);
			}
		}
	}
}
