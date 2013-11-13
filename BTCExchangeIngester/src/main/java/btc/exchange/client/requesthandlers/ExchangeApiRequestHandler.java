package btc.exchange.client.requesthandlers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import btc.exchange.client.BtcExchangeConfig;

import com.google.common.util.concurrent.AbstractScheduledService;

public abstract class ExchangeApiRequestHandler extends AbstractScheduledService {

	protected BtcExchangeConfig EXCHANGE_CONFIG;
	private static final PoolingHttpClientConnectionManager CM = new PoolingHttpClientConnectionManager();
    static {
    	CM.setMaxTotal(BtcExchangeConfig.values().length);
    }
	private final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(CM).build();
	

	protected abstract void handleResponse(String jsonResponse);
	protected abstract String getApiMethodPath();
	
	@Override
	protected final void runOneIteration() throws Exception {
		try {
			System.out.println(getHttpRequest());
			handleResponse(httpClient.execute(getHttpRequest(), DEFAULT_REQUEST_HANDLER));
		} catch (IOException e1) {
			e1.printStackTrace();
		} 		
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
			try {
				tickerHttpGet = new HttpGet( new URIBuilder(EXCHANGE_CONFIG.getBaseURI())
					.setPath(EXCHANGE_CONFIG.getBaseURI().getPath() + getApiMethodPath()).build() );
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return tickerHttpGet;
	}

}
