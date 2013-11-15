import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.primitives.Doubles;

public class VolatilityBand {

    // Algorithm for volatility band discussed in http://www.math.ucsd.edu/~politis/PAPER/VolBandsJTA.pdf
	
	// You can download entire exchange transaction histories from http://api.bitcoincharts.com/v1/csv/
	private static final String BTC_TRX_FILE = "ExchangeTradeHistory/mtgoxUSD.csv"; // ex. 1305349163,8.410010000000,11.618000000000
    private static final double STANDARD_DEVIATION_RATIO = 1.65; // http://en.wikipedia.org/wiki/Standard_normal_table
    private static long start;
    private static long end;
    
    public static void main(String[] args) throws IOException {
    	final double desiredDiscreteIntervalInMillis = 1000 * 60 * 60;
    	final ImmutableList<Double> weightedPriceIntervalAverages = getAveragedDiscreteIntervals(desiredDiscreteIntervalInMillis); 			
    			
        int sampleWindow = 10; // q
        int varianceSampleWindow=10; //Q
        final double predictionErrorVarianceStatistic = ( 1 + ( ((sampleWindow - 1) * (2*sampleWindow - 1)) / ( 6.0*sampleWindow ) ) ); // needed for equation 15
        
        final DescriptiveStatistics rollingTradePrices = new DescriptiveStatistics(sampleWindow);
        final DescriptiveStatistics rollingTradePricesForVarianceSample = new DescriptiveStatistics(varianceSampleWindow);
        // Pre-load the minimal samples (varianceSampleWindow, Q) before find the volatility bands
        for (int i = 0; i < varianceSampleWindow; i++) {
        	final double weightedPriceIntervalAverage = weightedPriceIntervalAverages.get(i);
        	rollingTradePrices.addValue(weightedPriceIntervalAverage);
        	rollingTradePricesForVarianceSample.addValue(weightedPriceIntervalAverage);
        }

        final SummaryStatistics volatilityBandSizes = new SummaryStatistics();
        final SummaryStatistics percentagePriceDiffsFromMean = new SummaryStatistics();

        final int numPredictions = (int) (weightedPriceIntervalAverages.size() - varianceSampleWindow);
        double lowerLimit = Double.MIN_VALUE;
        double upperLimit = Double.MAX_VALUE;
        int numPredictionsOutsideWindow = 0;
        
        for (int i = varianceSampleWindow; i < weightedPriceIntervalAverages.size(); i++) {
        	final double weightedPriceIntervalAverage = weightedPriceIntervalAverages.get(i);
            if ( weightedPriceIntervalAverage < lowerLimit || weightedPriceIntervalAverage > upperLimit) {
                numPredictionsOutsideWindow++;
            }
            
        	rollingTradePrices.addValue(weightedPriceIntervalAverage);
        	rollingTradePricesForVarianceSample.addValue(weightedPriceIntervalAverage);

        	final double sampleVariance = rollingTradePricesForVarianceSample.getVariance();
        	final double predictionVariance = sampleVariance * predictionErrorVarianceStatistic;
        	final double predictionStandardDeviation = Math.sqrt(predictionVariance);            
            final double deviation = STANDARD_DEVIATION_RATIO * predictionStandardDeviation;
            final double geometricMean = rollingTradePrices.getGeometricMean();
            lowerLimit = geometricMean - deviation;
            upperLimit = geometricMean + deviation;
            final double percentagePriceDiffFromMean = deviation / geometricMean * 100;
            percentagePriceDiffsFromMean.addValue(percentagePriceDiffFromMean);
            final double volatilityBandSize = upperLimit - lowerLimit;
            volatilityBandSizes.addValue(volatilityBandSize);
            
            System.out.println(geometricMean + " : " + upperLimit + " : " + lowerLimit + " : " + percentagePriceDiffFromMean);          
        }

        System.out.println(volatilityBandSizes.getMean() + " : average window size");
        System.out.println(percentagePriceDiffsFromMean.getMean() + " : average percent diff size");
        System.out.println(new Date(start) + "-" + new Date(end));
        final int numExpectedIntervals = (int)( (end - start)  / desiredDiscreteIntervalInMillis );
        System.out.println("Expected " +  numExpectedIntervals + " intervals : got " + weightedPriceIntervalAverages.size());
        final double predictionBandDistrubtionPercentage = ( 1 - ( numPredictionsOutsideWindow / (double) numPredictions )) * 100 ;
        System.out.println("Window distribution coverage % " + predictionBandDistrubtionPercentage);


    }
    
    static ImmutableList<Double> getAveragedDiscreteIntervals(final double intervalInMillis) throws IOException {
    	return Files.readLines(
      		  new File(BTC_TRX_FILE),
      		  Charsets.UTF_8,
      		  new LineProcessor<ImmutableList<Double>>() {
      			private final ImmutableList.Builder<Double> listOfAveragedDiscreteIntervalsBuilder = ImmutableList.builder();
      			private final Splitter niceCommaSplitter = Splitter.on(',').omitEmptyStrings().trimResults();
      			
      			private final List<Double> trades = new ArrayList<Double>();
      			private final List<Double> quantities = new ArrayList<Double>();
      			private final Mean weightedMeanStatistic = new Mean();
      		    
      		    private long intervalStartTime = 0;

      		    // 1305349163,8.410010000000,11.618000000000
      		    @Override public boolean processLine(String line) {   		    	
      		    	final List<String> tradeCols = niceCommaSplitter.splitToList(line);
      		    	final long unixMillis = Long.parseLong(tradeCols.get(0)) * 1000;
      		    	final Double price = Double.parseDouble(tradeCols.get(1));
      		    	final Double quantity =  Double.parseDouble(tradeCols.get(2));
      		    	
      		    	if ((unixMillis - intervalStartTime) >= intervalInMillis) {  // if we have passed our desired discrete interval update with the current mean
      		    		if (intervalStartTime == 0) { // initialization edge case
      		    			intervalStartTime = unixMillis;
      		    			start = intervalStartTime;
      		    		} else {
	      		    		final double weightedMean = weightedMeanStatistic.evaluate(Doubles.toArray(trades), Doubles.toArray(quantities));
	      		    		listOfAveragedDiscreteIntervalsBuilder.add(weightedMean);	
	      		    		
	      		    		// fill in any discrete interval gaps until the next transaction with the same computed mean.
	      		    		intervalStartTime += intervalInMillis;
	      		    		int gap = (int) Math.floor(((unixMillis - intervalStartTime)/intervalInMillis));
	      		    		for (int i = 0; i < gap; i++) {
	      		    			listOfAveragedDiscreteIntervalsBuilder.add(weightedMean);
	      		    			intervalStartTime += intervalInMillis;
	      		    		}
	      		    		
	      		    		// clear for the next discrete interval
	      		    		trades.clear();
	      		    		quantities.clear();
	      		    		end = unixMillis;
      		    		}
      		    	}
      		    	
      		    	trades.add(price);      		    	
      		    	quantities.add(quantity);
      		      return true;
      		    }
      		    @Override public ImmutableList<Double> getResult() {
      		      return listOfAveragedDiscreteIntervalsBuilder.build();
      		    }
      		  });
    }
}
