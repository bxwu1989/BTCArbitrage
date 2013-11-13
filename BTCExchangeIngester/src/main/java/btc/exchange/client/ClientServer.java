package btc.exchange.client;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.reflections.Reflections;

import btc.exchange.client.framework.ExchangeApiScheduledService;
import btc.exchange.client.requesthandlers.Exchange;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;

public class ClientServer {	

	public static void main(String[] args) throws InterruptedException {
		
		final ServiceManager manager = new ServiceManager(initializeAllExchangeScheduledServices());
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
	
	
	@SuppressWarnings("unchecked")
	private static List<Service> initializeAllExchangeScheduledServices() {
		final List<Service> allExchangesMarketRequests = new ArrayList<>();
		final Reflections reflections = new Reflections("btc.exchange.client");    		
		for ( final Class<?> exchangeRequestHandlersClass : reflections.getTypesAnnotatedWith(Exchange.class)) {
			final BtcExchangeConfig exchangeConfig = exchangeRequestHandlersClass.getAnnotation(Exchange.class).value();			
			for ( final Class<?> innerExchangeClass : exchangeRequestHandlersClass.getDeclaredClasses()) {
				if (innerExchangeClass.getSuperclass().equals(ExchangeApiScheduledService.class)) {
					try {
						final ExchangeApiScheduledService scheduledService = (ExchangeApiScheduledService) Class.forName(innerExchangeClass.getName()).newInstance();
						for ( final Field field : getAllFields(ExchangeApiScheduledService.class, withType(BtcExchangeConfig.class)) ) {							
							field.setAccessible(true);
							field.set(scheduledService, exchangeConfig);
						}
						allExchangesMarketRequests.add(scheduledService);
					} catch (InstantiationException | IllegalAccessException
							| ClassNotFoundException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		return allExchangesMarketRequests;
	}
}
