package btc.exchange.client.framework;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import btc.exchange.client.BtcExchangeConfig;

import com.google.common.util.concurrent.AbstractScheduledService;

public abstract class ExchangeApiScheduledService extends AbstractScheduledService {

	protected BtcExchangeConfig EXCHANGE_CONFIG;
	private static final PoolingHttpClientConnectionManager CM = new PoolingHttpClientConnectionManager();
    static {
    	CM.setMaxTotal(BtcExchangeConfig.values().length);
    }
	private final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(CM).build();
	
	// Would like to change this to just return a string of the API path so
			// that we can move the HttpRequest construction logic into this
			// framework. However in the future we will need to suppport Post and
			// Https requests as well.
	protected abstract HttpRequestBase getHttpRequest();
	protected abstract void handleResponse(String rawResponse);
	
	@Override
	protected final void runOneIteration() throws Exception {
		try {
			handleResponse(httpClient.execute(getHttpRequest(), DEFAULT_REQUEST_HANDLER));
		} catch (IOException e1) {
			e1.printStackTrace();
		} 		
	}
	
	private static final ResponseHandler<String> DEFAULT_REQUEST_HANDLER = new ResponseHandler<String>() {
        public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        }
    };

}
