package framework;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;

import exchange.client.IngestorServiceLoader;

public class ManagedServiceManager {	

	private final ServiceManager serviceManager;

	public ManagedServiceManager(ServiceLoader serviceLoader) {
		serviceManager = new ServiceManager(serviceLoader.getAllServices());
		serviceManager.addListener(new DefaultListener(), MoreExecutors.sameThreadExecutor());
		addShutdownHook();
	}
	
	public ManagedServiceManager(ServiceLoader serviceLoader, Listener serviceListener) {
		serviceManager = new ServiceManager(serviceLoader.getAllServices());
		serviceManager.addListener(serviceListener, MoreExecutors.sameThreadExecutor());
		addShutdownHook();
	}
	
	public void start() {
		serviceManager.startAsync();
	}
	
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				// Give the services 5 seconds to stop to ensure that we are
				// responsive to shutdown requests.
				try {
					serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
				} catch (TimeoutException timeout) {
					// stopping timed out
				}
			}
		});
	}
	
	private static class DefaultListener extends Listener {
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
	};
	
	public static void main(String[] args) throws InterruptedException {
		final ManagedServiceManager managedServicemanager = new ManagedServiceManager(IngestorServiceLoader.getInstance());
		managedServicemanager.start();
	}
	
	

}
