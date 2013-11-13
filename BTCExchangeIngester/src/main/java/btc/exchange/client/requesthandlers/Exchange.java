package btc.exchange.client.requesthandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import btc.exchange.client.BtcExchangeConfig;

@Retention(RetentionPolicy.RUNTIME)
public @interface Exchange {
	BtcExchangeConfig value();
}
