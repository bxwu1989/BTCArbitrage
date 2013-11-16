package prediction.models;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class VolatilityBand {

	private static final int DEFAULT_WINDOW_SIZE = 10;
	private static final int DEFAULT_VARIANCE_WINDOW_SIZE = 10;
	private static final double STANDARD_DEVIATION_RATIO = 1.96; // http://en.wikipedia.org/wiki/Standard_normal_table

	private final int rollingGeoMeanWindowSize;
	
	private final DescriptiveStatistics rollingTradePrices;
    private final DescriptiveStatistics rollingTradePricesForVarianceSample;
    
    final double predictionErrorVarianceStatistic; // needed for equation 15
    
    private double currentLowerPredictionBandLimit = Double.MIN_VALUE;
    private double currentUpperPredictionBandLimit = Double.MAX_VALUE;

    final SummaryStatistics meanBandPercentDiffFromMean = new SummaryStatistics();
    
	public VolatilityBand() {
		this.rollingGeoMeanWindowSize = DEFAULT_WINDOW_SIZE;
		rollingTradePrices = new DescriptiveStatistics(DEFAULT_WINDOW_SIZE);
		rollingTradePricesForVarianceSample = new DescriptiveStatistics(DEFAULT_VARIANCE_WINDOW_SIZE);
		predictionErrorVarianceStatistic = ( 1 + ( ( (DEFAULT_WINDOW_SIZE - 1) * (2*DEFAULT_WINDOW_SIZE - 1) ) / ( 6.0*DEFAULT_WINDOW_SIZE ) ) ); 
	}
	
	public VolatilityBand(int windowSize, int varianceWindowSize) {
		this.rollingGeoMeanWindowSize = windowSize;
		rollingTradePrices = new DescriptiveStatistics(windowSize);
		rollingTradePricesForVarianceSample = new DescriptiveStatistics(varianceWindowSize);
		predictionErrorVarianceStatistic = ( 1 + ( ((windowSize - 1) * (2*windowSize - 1)) / ( 6.0*windowSize ) ) ); 	
	}
	
	public int getWindowSize() {
		return rollingGeoMeanWindowSize;
	}
	
	public int getSizeOfSamples() {
		return (int) rollingTradePricesForVarianceSample.getN();
	}
	
	public int getSizeOfVarianceSamples() {
		return (int) rollingTradePricesForVarianceSample.getN();
	}
	
	public void warmSamples(double[] vals) {
		for (double val : vals) { 
			rollingTradePrices.addValue(val);
			rollingTradePricesForVarianceSample.addValue(val);
		}
		calcBandRange();
	}
	
	public void addValue(double val) {
		rollingTradePrices.addValue(val);
    	rollingTradePricesForVarianceSample.addValue(val);
    	calcBandRange();
	}

	private void calcBandRange() {
       // Once we have a minimum of Q samples start storing prediction band related stats
        if (getSizeOfVarianceSamples() >= rollingTradePricesForVarianceSample.getWindowSize()) {
        	final double sampleVariance = rollingTradePricesForVarianceSample.getVariance();
        	final double predictionVariance = sampleVariance * predictionErrorVarianceStatistic;
        	final double predictionStandardDeviation = Math.sqrt(predictionVariance);            
            final double deviation = STANDARD_DEVIATION_RATIO * predictionStandardDeviation;
            final double geometricMean = rollingTradePrices.getGeometricMean();
            final double percentDifferenceFromGeometricMean = deviation / geometricMean;
            
        	currentLowerPredictionBandLimit = geometricMean - deviation;
            currentUpperPredictionBandLimit = geometricMean + deviation;
            meanBandPercentDiffFromMean.addValue(percentDifferenceFromGeometricMean);
        }
	}
	
	public VolatilityBandRange getVolatilityBandRange() {
		return new VolatilityBandRange(currentLowerPredictionBandLimit, currentUpperPredictionBandLimit);
	}
	
	public boolean isOutOfRange(double val) {
		return ( val < currentLowerPredictionBandLimit || val > currentUpperPredictionBandLimit );
	}

	public double getBandPercentDiffFromMean() {
		return meanBandPercentDiffFromMean.getMean();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VolatilityBand [rollingGeoMeanWindowSize=")
				.append(rollingGeoMeanWindowSize)
				.append(", currentLowerPredictionBandLimit=")
				.append(currentLowerPredictionBandLimit)
				.append(", currentUpperPredictionBandLimit=")
				.append(currentUpperPredictionBandLimit)
				.append(", meanBandPercentDiffFromMean=")
				.append(meanBandPercentDiffFromMean.getMean()).append("]");
		return builder.toString();
	}
	
	public static class VolatilityBandRange {
		private final double lowerLimit;
        private final double upperLimit;
        
        public boolean isInRange(double val) {
    		return ( val < lowerLimit || val > upperLimit );
    	}
        
        @Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("VolatilityBandRange [lowerLimit=")
					.append(lowerLimit).append(", upperLimit=")
					.append(upperLimit).append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(lowerLimit);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(upperLimit);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VolatilityBandRange other = (VolatilityBandRange) obj;
			if (Double.doubleToLongBits(lowerLimit) != Double
					.doubleToLongBits(other.lowerLimit))
				return false;
			if (Double.doubleToLongBits(upperLimit) != Double
					.doubleToLongBits(other.upperLimit))
				return false;
			return true;
		}

		protected VolatilityBandRange(double lowerLimit, double upperLimit) {
        	this.lowerLimit = lowerLimit;
        	this.upperLimit = upperLimit;
        }

		public double getLowerLimit() {
			return lowerLimit;
		}

		public double getUpperLimit() {
			return upperLimit;
		}
	}
}
