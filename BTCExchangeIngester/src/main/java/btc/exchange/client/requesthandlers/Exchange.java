package btc.exchange.client.requesthandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import btc.exchange.client.ExchangeConfig;

@Retention(RetentionPolicy.RUNTIME)
public @interface Exchange {
	ExchangeConfig value();
}
