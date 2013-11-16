import java.io.File;
import java.io.IOException;
import java.util.List;

import prediction.models.IntervalManagedVolatilityBand;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class VolatilityBandTester {

    // Algorithm for volatility band discussed in http://www.math.ucsd.edu/~politis/PAPER/VolBandsJTA.pdf
	
	// You can download entire exchange transaction histories from http://api.bitcoincharts.com/v1/csv/
	private static final String BTC_TRX_FILE = "ExchangeTradeHistory/mtgoxUSD.csv"; // ex. 1305349163,8.410010000000,11.618000000000

    public static void main(String[] args) throws IOException {
    	final double desiredDiscreteIntervalInMillis = 1000 * 60 * 60;
    	
    	final IntervalManagedVolatilityBand vBand = readExchangeTransactions(desiredDiscreteIntervalInMillis);
 	
    	System.out.println(vBand);
    	
        final int numExpectedIntervals = (int)( (vBand.getLatestTimetamp() - vBand.getEpoch())  / desiredDiscreteIntervalInMillis );
        System.out.println("Expected " +  numExpectedIntervals + " intervals : got " + vBand.getIntervalsSinceEpoch());
    }
    	
	static IntervalManagedVolatilityBand readExchangeTransactions(final double intervalInMillis) throws IOException {
		return Files.readLines(new File(BTC_TRX_FILE), Charsets.UTF_8,
				new LineProcessor<IntervalManagedVolatilityBand>() {
					private final Splitter niceCommaSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
					private IntervalManagedVolatilityBand vBand = new IntervalManagedVolatilityBand(intervalInMillis);

					// 1305349163,8.410010000000,11.618000000000
					@Override
					public boolean processLine(String line) {
						final List<String> tradeCols = niceCommaSplitter.splitToList(line);
						final long unixMillis = Long.parseLong(tradeCols.get(0)) * 1000;
						final Double price = Double.parseDouble(tradeCols.get(1));
						final Double quantity = Double.parseDouble(tradeCols.get(2));

						vBand.addVal(unixMillis, price, quantity);

						return true;
					}

					@Override
					public IntervalManagedVolatilityBand getResult() {
						return vBand;
					}
				});
	}
}
