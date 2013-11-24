package exchange.configuration;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import exchange.Exchange;
import exchange.InnerExchange;
import exchange.currency.Currency;
import exchange.currency.CurrencyQuantDelegate;

public class ExchangeFeeConfig {

	private static Map<Exchange, Map<InnerExchange, Fee>> TRADE_COMMISSIONS;
	private static Map<Exchange, Map<Currency, Fee>> TRANSFER_FEES;
	
	static {
		final Map<Exchange, Fee> DEFAULT_COMMISSION_FEES = new EnumMap<>(Exchange.class);
		final Map<Exchange, Fee> DEFAULT_TRANSFER_FEES = new EnumMap<>(Exchange.class);
		
		// Define default commission fees here:
		DEFAULT_COMMISSION_FEES.put(Exchange.CAMPBX, Fee.getInstance(0, .55)); // Fee.getInstance(FLAT_RATE, PERCENTAGE_RATE)
		DEFAULT_COMMISSION_FEES.put(Exchange.BITFINIX, Fee.getInstance(0, .12));
		DEFAULT_COMMISSION_FEES.put(Exchange.BITSTAMP, Fee.getInstance(0, .5));
//		DEFAULT_COMMISSION_FEES.put(Exchange.MTGOX, Fee.getInstance(0, .6));
		
		// Define default transfer fees here:
		DEFAULT_TRANSFER_FEES.put(Exchange.CAMPBX, Fee.getInstance(0, 0)); 
		DEFAULT_TRANSFER_FEES.put(Exchange.BITFINIX, Fee.getInstance(0, 0));
		DEFAULT_TRANSFER_FEES.put(Exchange.BITSTAMP, Fee.getInstance(0, 0));
//		DEFAULT_TRANSFER_FEES.put(Exchange.MTGOX, Fee.getInstance(0, 0));
		
		buildExchangeCommisionFeeMap(DEFAULT_COMMISSION_FEES);
		buildExchangeTransferFeeMap(DEFAULT_TRANSFER_FEES);
			
		// Trade Commission Overrides
		TRADE_COMMISSIONS.get(Exchange.CAMPBX).put(InnerExchange.BTC_USD, Fee.getInstance(0, .55));
				
		// Transfer Fee Overrides
		TRANSFER_FEES.get(Exchange.CAMPBX).put(Currency.BTC, Fee.getInstance(0, 0));
		
	}
	
	public static Fee getTradeCommissionFee(Exchange exchange, InnerExchange innerExchange) {
		return TRADE_COMMISSIONS.get(exchange).get(innerExchange);
	}
	
	public static Fee getTransferFee(Exchange exchange, Currency currency) {
		return TRANSFER_FEES.get(exchange).get(currency);
	}
	
	public static class Fee {
		private static final Map<Integer, Fee> flywieghtFees = new HashMap<>();
		
		private final CurrencyQuantDelegate flatRate;
		private final CurrencyQuantDelegate percentage;
		
		public static Fee getInstance(double flatRate, double percentage) {
			Fee fee = flywieghtFees.get(feeHashCode(flatRate, percentage));
			if (fee == null) {
				fee = new Fee(flatRate, percentage);
				flywieghtFees.put(feeHashCode(flatRate, percentage), fee);
			}
			return fee;
		}

		private static int feeHashCode(double flat, double percentage) {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(flat);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(percentage);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
		
		private Fee(double flat, double percentage) {
			this.flatRate = CurrencyQuantDelegate.getCurrencyQuant(flat);
			this.percentage = CurrencyQuantDelegate.getCurrencyQuant(percentage);
		}

		public CurrencyQuantDelegate getFlatRate() {
			return flatRate;
		}

		public CurrencyQuantDelegate getPercentage() {
			return percentage;
		}
		
		public CurrencyQuantDelegate getAppliedFeeQuantity(CurrencyQuantDelegate quantity) {
			return quantity.multiply(percentage).add(flatRate);
		}
		
		public CurrencyQuantDelegate getQuantityAfterAppliedFee(CurrencyQuantDelegate quantity) {
			return quantity.subtract(getAppliedFeeQuantity(quantity));
		}
	}
	
	private static void buildExchangeCommisionFeeMap(final Map<Exchange, Fee> defaults) {
		if (TRADE_COMMISSIONS != null) throw new IllegalStateException("Only meant to be initialized once.");
		final ImmutableMap.Builder<Exchange, Map<InnerExchange, Fee>> builder = ImmutableMap.builder();
		for (Entry<Exchange, Fee> entry : defaults.entrySet()) {
			final Exchange ex = entry.getKey();
			final Map<InnerExchange, Fee> innerExchangeCommisionFeeMap = new EnumMap<>(InnerExchange.class);
			for (InnerExchange innerEx : ExchangeExchangeConfig.getInnerExchanges(ex)) {
				innerExchangeCommisionFeeMap.put(innerEx, defaults.get(ex));
			}
			builder.put(ex, innerExchangeCommisionFeeMap);
		}
		TRADE_COMMISSIONS = builder.build();
	}
	
	private static void buildExchangeTransferFeeMap(final Map<Exchange, Fee> defaults) {
		if (TRANSFER_FEES != null) throw new IllegalStateException("Only meant to be initialized once.");
		final ImmutableMap.Builder<Exchange, Map<Currency, Fee>> builder = ImmutableMap.builder();
		for (Entry<Exchange, Fee> entry : defaults.entrySet()) {
			final Exchange ex = entry.getKey();
			final Map<Currency, Fee> currencyTransferFeeMap = new EnumMap<>(Currency.class);
			for (Currency currency : ExchangeExchangeConfig.getExchangeCurrencies(ex)) {
				switch(currency.getType()) {
				case Non_Crypto:
				case Crypto:
					currencyTransferFeeMap.put(currency, defaults.get(ex));
					break;
				case Fiat:
					default:
				}
				
			}
			builder.put(ex, currencyTransferFeeMap);
		}
		TRANSFER_FEES = builder.build();
	}
	private ExchangeFeeConfig() {}
}
