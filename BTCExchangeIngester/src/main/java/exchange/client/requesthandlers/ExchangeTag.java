package exchange.client.requesthandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import exchange.client.Exchange;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExchangeTag {
	Exchange value();
}
