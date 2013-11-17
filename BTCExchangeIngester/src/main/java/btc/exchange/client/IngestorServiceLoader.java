package btc.exchange.client;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;

import btc.exchange.client.requesthandlers.Exchange;
import btc.exchange.client.requesthandlers.ExchangeApiRequestHandler;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;

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
		final Reflections reflections = new Reflections("btc.exchange.client");    		
		for ( final Class<?> exchangeRequestHandlersClass : reflections.getTypesAnnotatedWith(Exchange.class)) {
			final ExchangeConfig exchangeConfig = exchangeRequestHandlersClass.getAnnotation(Exchange.class).value();			
			for ( final Class<?> innerExchangeClass : exchangeRequestHandlersClass.getDeclaredClasses()) {
				if (innerExchangeClass.getSuperclass().equals(ExchangeApiRequestHandler.class)) {
					try {
						final ExchangeApiRequestHandler scheduledService = (ExchangeApiRequestHandler) Class.forName(innerExchangeClass.getName()).newInstance();
						for ( final Field field : getAllFields(ExchangeApiRequestHandler.class, withType(ExchangeConfig.class)) ) {							
							field.setAccessible(true);
							field.set(scheduledService, exchangeConfig);
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
		final Reflections reflections = new Reflections("btc.exchange.client.publishers");   
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
