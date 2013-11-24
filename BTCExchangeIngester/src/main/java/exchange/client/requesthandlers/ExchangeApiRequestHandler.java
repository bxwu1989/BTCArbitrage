package exchange.client.requesthandlers;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.google.common.util.concurrent.AbstractScheduledService;

import exchange.client.Exchange;

public abstract class ExchangeApiRequestHandler extends AbstractScheduledService {

	protected Exchange exchange;
	private Scheduler timeoutScheduler;
	private String connectionString;
	private static final PoolingHttpClientConnectionManager CM = new PoolingHttpClientConnectionManager();
    static {
    	CM.setMaxTotal(Exchange.values().length);
    }
	private final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(CM).build();
	

	protected abstract void handleResponse(String jsonResponse);
	protected abstract String getApiMethodPath();
	
	@Override
	protected final void runOneIteration() throws Exception {
		try {
			handleResponse(httpClient.execute(getHttpRequest(), DEFAULT_REQUEST_HANDLER));
		} catch (IOException e1) {
			e1.printStackTrace();
		} 		
	}
	
	@Override
	protected Scheduler scheduler() {
		return timeoutScheduler; // TODO pull from config
	}
	
	private static final ResponseHandler<String> DEFAULT_REQUEST_HANDLER = new ResponseHandler<String>() {
		
        public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
            final int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                final HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    };
    
	/**
	 * @Override this method if you need to issue a different request other than GET, e.g. POST, or need configure with additional parameters
	 * 
	 * TODO try to remove the need to @Override for separate types of requests.
	 */
	private HttpGet tickerHttpGet;	
	protected HttpRequestBase getHttpRequest() {
		if (tickerHttpGet == null) {
			tickerHttpGet = new HttpGet( connectionString + getApiMethodPath() );
		}
		return tickerHttpGet;
	}

}
