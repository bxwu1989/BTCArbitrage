package exchange.client.requesthandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import exchange.client.ExchangeConfig;

@Retention(RetentionPolicy.RUNTIME)
public @interface Exchange {
	ExchangeConfig value();
}
