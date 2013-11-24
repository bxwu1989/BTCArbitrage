package exchange.services.requesthandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import exchange.Exchange;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExchangeTag {
	Exchange value();
}
