package btc.exchange.client.framework;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import btc.exchange.client.BtcExchangeConfig;

import com.google.common.util.concurrent.AbstractScheduledService;

public abstract class ExchangeApiScheduledService extends AbstractScheduledService {

	private static final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    static {
    	cm.setMaxTotal(BtcExchangeConfig.values().length);
    }
	private final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
	
	public ExchangeApiScheduledService() {
	}
	
	protected abstract HttpGet getHttpGet();
	protected abstract void handleResponse(String rawResponse);
	
	@Override
	protected final void runOneIteration() throws Exception {
		try {
			handleResponse(httpClient.execute(getHttpGet(), DEFAULT_REQUEST_HANDLER));
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
