package exchange.services;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withType;
import static org.reflections.ReflectionUtils.withName;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.Service;

import exchange.Exchange;
import exchange.services.requesthandlers.ExchangeApiRequestHandler;
import exchange.services.requesthandlers.ExchangeTag;
import exchange.configuration.ExchangeApiConfig;
import framework.ServiceLoader;

public class IngestorServiceLoader implements ServiceLoader {

	private final List<Service> ALL_SERVICES = new ArrayList<Service>();
	private static final IngestorServiceLoader serviceLoader = new IngestorServiceLoader();
	private IngestorServiceLoader() {
		initializeAllExchangeScheduledServices();
		initializeAllPublishers();
	}
	
	public static IngestorServiceLoader getInstance() {
		return serviceLoader;
	}
	
	public List<Service> getAllServices() {
		return ALL_SERVICES;
	}
	
	@SuppressWarnings("unchecked")
	private void initializeAllExchangeScheduledServices() {
		final Reflections reflections = new Reflections("exchange.services");    		
		for ( final Class<?> exchangeRequestHandlersClass : reflections.getTypesAnnotatedWith(ExchangeTag.class)) {
			
			final Exchange exchange = exchangeRequestHandlersClass.getAnnotation(ExchangeTag.class).value();	
			
			for ( final Class<?> innerExchangeClass : exchangeRequestHandlersClass.getDeclaredClasses()) {
				
				if (innerExchangeClass.getSuperclass().equals(ExchangeApiRequestHandler.class)) {
					try {
						final ExchangeApiRequestHandler scheduledService = (ExchangeApiRequestHandler) Class.forName(innerExchangeClass.getName()).newInstance();
						for ( final Field field : getAllFields(ExchangeApiRequestHandler.class, withType(Exchange.class)) ) {							
							field.setAccessible(true);
							field.set(scheduledService, exchange);
						}
						for ( final Field field : getAllFields(ExchangeApiRequestHandler.class, withType(Scheduler.class)) ) {							
							field.setAccessible(true);
							field.set(scheduledService, ExchangeApiConfig.getDefaultApiTimeoutScheduler(exchange));
						}
						for ( final Field field : getAllFields(ExchangeApiRequestHandler.class, withType(String.class), withName("connectionString")) ) {							
							field.setAccessible(true);
							field.set(scheduledService, ExchangeApiConfig.getApiString(exchange));
						}
						ALL_SERVICES.add(scheduledService);
					} catch (InstantiationException | IllegalAccessException
							| ClassNotFoundException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
	
	private void initializeAllPublishers() {
		final Reflections reflections = new Reflections("exchange.services.publishers");   
		for ( final Class<? extends AbstractScheduledService> scheduledServicePublishers : reflections.getSubTypesOf(AbstractScheduledService.class)) {
			if (!Modifier.isAbstract( scheduledServicePublishers.getModifiers() )) {
				try {
					ALL_SERVICES.add(scheduledServicePublishers.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
