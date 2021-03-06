package exchange.services;

import util.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;

public class AWSClients {

	private static final String DEFUALT_ENDPOINT = Configuration.getInstance().getProperty("endpoint");
	
	private static final AmazonSNSClient SNS_CLIENT;	
	static {
		final AWSCredentials creds = new BasicAWSCredentials(getKey(), getSecret());
		SNS_CLIENT = new AmazonSNSClient(creds);
		SNS_CLIENT.setEndpoint(DEFUALT_ENDPOINT);
	}
	
	public static AmazonSNSClient getSNSClient() {
		return SNS_CLIENT;
	}
	
	private static String getKey () {
		return Configuration.getInstance().getProperty("accessKey");
	}

	private static String getSecret () {
		return Configuration.getInstance().getProperty("secretKey");
	}
	
	private AWSClients() {
	}
}
