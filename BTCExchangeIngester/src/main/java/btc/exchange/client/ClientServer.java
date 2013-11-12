package btc.exchange.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import btc.exchange.client.BtcExchangeConfig;
import btc.exchange.client.ExchangeClient;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager.Listener;

public class ClientServer {

	
	public static void main(String[] args) throws InterruptedException {
		CampBXExchangeClient.init();
		final List<Service> allExchangesMarketRequests = new ArrayList<>();
		for (final ExchangeClient exClient: BtcExchangeConfig.getAllExchangeClients()) {
			allExchangesMarketRequests.addAll(exClient.getBtcExchangeMarketRequests());
		}
		
		final ServiceManager manager = new ServiceManager(allExchangesMarketRequests);
		manager.addListener(new Listener() {
			public void stopped() {
			}

			public void healthy() {
				// Services have been initialized and are healthy, start
				// accepting requests...
			}

			public void failure(Service service) {
				// Something failed, at this point we could log it, notify a
				// load balancer, or take
				// some other action. For now we will just exit.
				System.exit(1);
			}
		}, MoreExecutors.sameThreadExecutor());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// Give the services 5 seconds to stop to ensure that we are
				// responsive to shutdown
				// requests.
				try {
					manager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
				} catch (TimeoutException timeout) {
					// stopping timed out
				}
			}
		});
		manager.startAsync();

		manager.awaitHealthy();
		System.out.println("started healthy.");
		
		for (;;) {
			for (Entry<State, Service> entries : manager.servicesByState().entries()) {
				System.out.println(entries);
			}
			Thread.sleep(1000);
		}
	}
}
