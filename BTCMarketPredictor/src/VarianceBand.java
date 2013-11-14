import java.util.ArrayList;
import java.util.List;

public class VarianceBand {

    // algorithm for variance band discussed in http://www.math.ucsd.edu/~politis/PAPER/VolBandsJTA.pdf

    private static final double INVERSE_COVERAGE_LEVEL = .05;
    private static final double STANDARD_DEVIATION_RATIO = 1.96; // http://en.wikipedia.org/wiki/Standard_normal_table

    public static void main(String[] args) {
        int sampleWindow = 10;
        int varianceSampleWindow=10;
        int minSamplesTOStart = varianceSampleWindow;
        // create List<double> from hourly weighted price averages by amount.
        final List<Double> weightedHourlyAverages = new ArrayList<Double>();
        // create a descriptivestatistic with window size sampleWindow
        // create a descriptiveStatistic with window size varianceSampleWindow
        // populate at least minSamplesToStart -1

        double lowerLimit = 0;
        double upperLimit = 99999;
        int numSamplesOutsideWindow = 0;
        final int numSamples = weightedHourlyAverages.size() - minSamplesTOStart - 1;
        for (Double weightedHourlyAverage : weightedHourlyAverages)    {
            if ( weightedHourlyAverage < lowerLimit || weightedHourlyAverage > upperLimit) {
                numSamplesOutsideWindow++;
            }
            //update descriptivestatistics
            // get sample variance
            double sampleVariance = 0;
            double a = sampleVariance * ( 1 + ( ( sampleWindow - 1)  * ( 2*sampleWindow - 1) / ( 6*sampleWindow ) ));
            // get geometic mean from sample descriptivestatistic
            double geometricMean = 0;
            lowerLimit = geometricMean * Math.exp(-STANDARD_DEVIATION_RATIO * a);
            upperLimit = geometricMean * Math.exp(STANDARD_DEVIATION_RATIO * a);

            System.out.println();
        }
        double predictionBandDistrubtionPercentage = ( numSamplesOutsideWindow / numSamples ) * 100;



    }
}
