package prediction.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

import prediction.models.VolatilityBand.VolatilityBandRange;

import com.google.common.collect.EvictingQueue;
import com.google.common.primitives.Doubles;

public class IntervalManagedVolatilityBand  {
	
	private final VolatilityBand vBand;
	private final double intervalInMillis;
	private long intervalStartTime = 0;
	private long epoch = 0;
	private long intervalsSinceEpoch = 0;
	private long latestTimetamp = 0;
	
	private final List<Double> trades = new ArrayList<Double>();
	private final List<Double> quantities = new ArrayList<Double>();
	private final Mean weightedMeanStatistic = new Mean();
		
	private long numValsAdded = 0;
	private long numValsObservedOutOfBand;
	
	private static final int MAX_INTERVAL_HISTORY = 2000;
	private final EvictingQueue<VolatilityBandRange> vBandHistory = EvictingQueue.create(MAX_INTERVAL_HISTORY);
	private final DescriptiveStatistics meanIntervalHistory = new DescriptiveStatistics(MAX_INTERVAL_HISTORY);
	
	public IntervalManagedVolatilityBand(double intervalInMillis) {
		this.intervalInMillis = intervalInMillis;
		vBand = new VolatilityBand();
	}

	public void addVal(final long unixMillis, final Double price, final Double quantity) {
		if (vBand.isOutOfRange(price)) {
			numValsObservedOutOfBand++;
        }
		
		// if we have passed our desired discrete interval update with the current mean
		if ((unixMillis - intervalStartTime) >= intervalInMillis) {
			if (intervalStartTime == 0) { // initialization edge case
				epoch = intervalStartTime = unixMillis;
			} else {
				final double weightedMean = weightedMeanStatistic.evaluate(
						Doubles.toArray(trades), Doubles.toArray(quantities));

				// fill in any discrete interval gaps until the next transaction
				// with the same computed mean.
				for (long currentIntervalStart = intervalStartTime; currentIntervalStart < (unixMillis - intervalInMillis); currentIntervalStart += intervalInMillis) {
					vBand.addValue(weightedMean);
					meanIntervalHistory.addValue(weightedMean);
					vBandHistory.add(vBand.getVolatilityBandRange());
					intervalStartTime += intervalInMillis;
					intervalsSinceEpoch++;
				}

				// clear for the next discrete interval
				trades.clear();
				quantities.clear();
			}
		}
		trades.add(price);      		    	
	    quantities.add(quantity);
	    latestTimetamp = unixMillis;
	    numValsAdded++;
	}
	
	public VolatilityBandRange getVolatilityBandRange() {
		return vBand.getVolatilityBandRange();
	}

	public double getBandPercentDiffFromMean() {
		return vBand.getBandPercentDiffFromMean();
	}

	public long getIntervalsSinceEpoch() {
		return intervalsSinceEpoch;
	}

	public long getLatestTimetamp() {
		return latestTimetamp;
	}

	public long getEpoch() {
		return epoch;
	}
	
	public double getPredictionIntervalHitRatePerValAdded() {
		return ( 1 - ( numValsObservedOutOfBand / (double) numValsAdded ));
	}
	
	public double getRelativePredictionIntervalHitRateSmoothed(long futureWindowLengthInMillis) {	
		int numIntervals = (int) Math.ceil((futureWindowLengthInMillis / (double) intervalInMillis));	
		return getRelativePredictionIntervalHitRateSmoothed(numIntervals);
	}
	
	// TODO is there a way to store this without having to calculate per request,
	// but still be able to support any desired user supplied interval?
	public double getRelativePredictionIntervalHitRateSmoothed(int numIntervals) {
		long numValsOutsideOfCurrentWindow = 0;
		final double numMeanIntervalVals = meanIntervalHistory.getN()-numIntervals;
		
		final Iterator<VolatilityBandRange> iter = vBandHistory.iterator();
		for (int i = 0; i < numMeanIntervalVals; i++) {
			final VolatilityBandRange vbRange = iter.next();
			if ( vbRange.isInRange( meanIntervalHistory.getElement( (i+numIntervals) ) ) ) {
				numValsOutsideOfCurrentWindow++;
			}
		}

		return ( 1 - ( numValsOutsideOfCurrentWindow / numMeanIntervalVals ));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IntervalManagedVolatilityBand [vBand=").append(vBand)
				.append(", intervalInMillis=").append(intervalInMillis)
				.append(", epoch=").append(new Date(epoch))
				.append(", intervalsSinceEpoch=").append(intervalsSinceEpoch)
				.append(", latestTimetamp=").append(new Date(latestTimetamp))
				.append(", numValsAdded=").append(numValsAdded)
				.append(", percentage of vals inside of band prediction=")
				.append(getPredictionIntervalHitRatePerValAdded())
				.append(", 2nd hour averaged intervals inside of band prediction=").append(getRelativePredictionIntervalHitRateSmoothed(2)).append("]");
		return builder.toString();
	}
}
