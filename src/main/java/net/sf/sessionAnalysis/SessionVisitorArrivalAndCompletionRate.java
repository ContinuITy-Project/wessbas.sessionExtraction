package net.sf.sessionAnalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
public class SessionVisitorArrivalAndCompletionRate implements ISessionDatVisitor {
	private final int resolutionValueOriginal;
	private final TimeUnit resolutionTimeUnitOriginal;
	private final long resolutionValueNanos;

	private volatile long minTimestampNanos = Long.MAX_VALUE;
	private volatile long maxTimestampNanos = Long.MIN_VALUE;

	private final List<Long> arrivalTimestamps = new LinkedList<Long>();
	private final List<Long> completionTimestamps = new LinkedList<Long>();
	private final List<SessionInformation> sessionDurationList = new LinkedList<SessionInformation>();

	private volatile long[] arrivalTimestampsSorted = null;
	private volatile long[] completionTimestampsSorted = null;
	private volatile long[] sessionDurationRates = null;
	

	private volatile int[] arrivalRates = null;
	private volatile int[] completionRates = null;
	private volatile double[] userActionRates = null;
	
	
	private volatile TreeMap<Long, Integer> numConcurrentSessionsOverTime =
			new TreeMap<Long, Integer>();
	private int[] maxNumSessionsPerInterval;

	public SessionVisitorArrivalAndCompletionRate(final int resolutionValue, final TimeUnit resolutionTimeUnit) {
		this.resolutionValueOriginal = resolutionValue;
		this.resolutionTimeUnitOriginal = resolutionTimeUnit;
		this.resolutionValueNanos = TimeUnit.NANOSECONDS.convert(this.resolutionValueOriginal, this.resolutionTimeUnitOriginal);
	}

	public void handleSession(Session session) {
		this.arrivalTimestamps.add(session.getSessionStartTimeStamp());
		this.completionTimestamps.add(session.getSessionEndTimeStamp());
		this.sessionDurationList.add(new SessionInformation(session.getSessionStartTimeStamp(), session.getSessionEndTimeStamp() - session.getSessionStartTimeStamp(), session.getUserActions().size()));
		if (session.getSessionStartTimeStamp() < this.minTimestampNanos) {
			this.minTimestampNanos = session.getSessionStartTimeStamp();
		}
		if (session.getSessionEndTimeStamp() > this.maxTimestampNanos) {
			this.maxTimestampNanos = session.getSessionEndTimeStamp();
		}
	}

	public void handleEOF() {
		arrivalTimestampsSorted = ArrayUtils.toPrimitive(arrivalTimestamps.toArray(new Long[] {}));
		Arrays.sort(arrivalTimestampsSorted);
		completionTimestampsSorted = ArrayUtils.toPrimitive(completionTimestamps.toArray(new Long[] {}));
		Arrays.sort(completionTimestampsSorted);
		this.computeRates();
		this.computeNumConcurrentSessionsOverTime();
		this.computeMaxSessionsPerInterval();
	}

	private void computeNumConcurrentSessionsOverTime() {
		int nextIdxArrivals = 0;
		int nextIdxCompletions = 0;

		long nextTimestamp = Math.min(arrivalTimestampsSorted[nextIdxArrivals], completionTimestampsSorted[nextIdxCompletions]);
		long currentTimestamp = nextTimestamp;

		int curNumConcurrentSessions = 0;

		boolean moreArrivals = (nextIdxArrivals < arrivalTimestampsSorted.length);
		boolean moreCompletions = (nextIdxCompletions < completionTimestampsSorted.length);

		// Each loop iteration processes one timestamp at which we observed (one or more) arrivals and completions
		while (moreArrivals || moreCompletions) {
			while (currentTimestamp == nextTimestamp) { // process current timestamp
				if (!moreCompletions || (moreArrivals && (arrivalTimestampsSorted[nextIdxArrivals] <= completionTimestampsSorted[nextIdxCompletions]))) { // note:
																																							// moreCompletions
																																							// ==
																																							// true
					curNumConcurrentSessions++;
					if (nextIdxArrivals == (arrivalTimestampsSorted.length - 1)) {
						moreArrivals = false;
					} else {
						nextIdxArrivals++;
					}
				} else { // note: moreCompletions == true; i.e., remaining case: !moreArrivals || next arrival > next completion
					curNumConcurrentSessions--;
					if (nextIdxCompletions == (completionTimestampsSorted.length - 1)) {
						moreCompletions = false;
					} else {
						nextIdxCompletions++;
					}
				}

				// Process time to timestamp of next event
				if (!moreArrivals && moreCompletions) {
					nextTimestamp = completionTimestampsSorted[nextIdxCompletions];
				} else if (moreArrivals && !moreCompletions) {
					nextTimestamp = arrivalTimestampsSorted[nextIdxArrivals];
				} else if (moreArrivals && moreCompletions) {
					nextTimestamp = Math.min(arrivalTimestampsSorted[nextIdxArrivals], completionTimestampsSorted[nextIdxCompletions]);
				} else { // !moreArrivals && !moreCompletions
					nextTimestamp = -1; // This forces the loop to terminate
				}
			}
			this.numConcurrentSessionsOverTime.put(currentTimestamp, curNumConcurrentSessions); // store value for elapsed timestamp
			currentTimestamp = nextTimestamp;
		}
	}

	private void computeRates() {
		final long durationNanos = this.maxTimestampNanos - this.minTimestampNanos;
		final int numBuckets = (int) Math.ceil((double) durationNanos / this.resolutionValueNanos);

		this.arrivalRates = new int[numBuckets];
		this.completionRates = new int[numBuckets];
		this.sessionDurationRates = new long[numBuckets];
		this.userActionRates = new double[numBuckets];

		for (long arrivalTimeStamp : this.arrivalTimestamps) {
			final int arrivalTimeStampBucket = (int) ((arrivalTimeStamp - this.minTimestampNanos) / this.resolutionValueNanos);
			arrivalRates[arrivalTimeStampBucket]++;
			for (SessionInformation sessionInformation : this.sessionDurationList) {
				if (sessionInformation.getTimestamp() == arrivalTimeStamp) {
					sessionDurationRates[arrivalTimeStampBucket] += sessionInformation.getDuration();
					userActionRates[arrivalTimeStampBucket] += sessionInformation.getCntUserActions();
				}
			}
		}

		for (long completionTimeStamp : this.completionTimestamps) {
			final int completionTimeStampBucket = (int) ((completionTimeStamp - this.minTimestampNanos) / this.resolutionValueNanos);
			completionRates[completionTimeStampBucket]++;
		}
		
		for (int i = 0; i < sessionDurationRates.length; i++) {
			if (arrivalRates[i] == 0) {
				sessionDurationRates[i] = 0;
			} else {
				sessionDurationRates[i] = sessionDurationRates[i] / arrivalRates[i] ;
			}
		}
		
		for (int i = 0; i < userActionRates.length; i++) {
			if (arrivalRates[i] == 0) {
				userActionRates[i] = 0;
			} else {
				userActionRates[i] = userActionRates[i] / (double) arrivalRates[i] ;
			}
		}
		
	}

	/**
	 * Note that metrics other than max, min are more difficult, as we need to include the duration
	 * that this number of sessions is present in the interval (e.g., for mean, median, ...).
	 */
	private void computeMaxSessionsPerInterval() {
		final long durationNanos = this.maxTimestampNanos - this.minTimestampNanos;
		final int numBuckets = (int) Math.ceil((double) durationNanos / this.resolutionValueNanos);

		// Note that the map must not be filled with 0's, because this introduces errors for 
		// intervals without events.
		TreeMap<Integer,Integer> numSessionsPerInterval = new TreeMap<Integer, Integer>(); 
		
		for (Entry<Long, Integer> numSessionChangeEvent : this.numConcurrentSessionsOverTime.entrySet()) {
			final long eventTimeStamp = numSessionChangeEvent.getKey();
			final int numSessionsAtTime = numSessionChangeEvent.getValue();
	
			final int eventTimeStampBucket = (int) ((eventTimeStamp - this.minTimestampNanos) / this.resolutionValueNanos);
			
			Integer lastMaxNumForBucket = numSessionsPerInterval.get(eventTimeStampBucket); 
			if (lastMaxNumForBucket == null || numSessionsAtTime > lastMaxNumForBucket) {
				numSessionsPerInterval.put(eventTimeStampBucket, numSessionsAtTime);
			}			
		}
		
		// Now we need to fill intervals without values with the last non-null value
		int lastNonNullValue = 0;
		for (int i=0; i<numBuckets; i++) {
			Integer curVal = numSessionsPerInterval.get(i); 
			if (curVal == null) {
				numSessionsPerInterval.put(i, lastNonNullValue);
			} else {
				lastNonNullValue = curVal;
			}
		}
		
		this.maxNumSessionsPerInterval = ArrayUtils.toPrimitive(numSessionsPerInterval.values().toArray(new Integer[0]));
	}
	
	public int[] getArrivalRates() {
		return this.arrivalRates;
	}
	
	public long[] getSessionDuration() {
		return this.sessionDurationRates;
	}

	public double[] getUserActionRates() {
		return this.userActionRates;
	}
	
	public int[] getCompletionRates() {
		return this.completionRates;
	}

	public long[] getArrivalTimestamps() {
		return ArrayUtils.toPrimitive(arrivalTimestamps.toArray(new Long[] {}));
	}

	public long[] getCompletionTimestamps() {
		return ArrayUtils.toPrimitive(completionTimestamps.toArray(new Long[] {}));
	}

	public int[] getMaxNumSessionsPerInterval() {
		return maxNumSessionsPerInterval;
	}
	
	/**
	 * Note that this is an event-based representation, i.e., the number of concurrent sessions is reported for every point in time where (one or more) sessions
	 * start or complete.
	 * 
	 * 
	 * @return
	 */
	public TreeMap<Long, Integer> getNumConcurrentSessionsOverTime() {
		return numConcurrentSessionsOverTime;
	}
	
	
	public void writeArrivalCompletionRatesAndMaxNumSessions(final String outputdir) throws IOException  {
		FileWriter fw = new FileWriter(outputdir + "/" + this.getClass().getSimpleName()+"-arrivalCompletionRatesAndMaxNumSessions.csv");
        BufferedWriter writer = new BufferedWriter(fw);
        
        writer.write("timestamp;arrivalRate;completionRate;maxConcurrentSessions");
        writer.newLine();
        int numBuckets = this.completionRates.length;
        for (int i=0; i<numBuckets; i++) {
        	writer.write((minTimestampNanos + i * (resolutionValueNanos)) + ";" + this.arrivalRates[i] + ";" + this.completionRates[i] + ";" + this.maxNumSessionsPerInterval[i]);
        	writer.newLine();
        }
        writer.close();
        fw.close();
	}
	
	public void writeSessionsOverTime(final String outputDir) throws IOException  {
		FileWriter fw = new FileWriter(outputDir + "/" + this.getClass().getSimpleName()+"-sessionsOverTime.csv");
        BufferedWriter writer = new BufferedWriter(fw);
        
        writer.write("timestamp;numSessions");
        writer.newLine();
        for (Entry<Long, Integer> event: this.numConcurrentSessionsOverTime.entrySet()) {
        	writer.write(event.getKey() + ";" + event.getValue());
        	writer.newLine();
        }
        
        writer.close();
        fw.close();
	}
	
	class SessionInformation {
		long timestamp = 0;
		long duration = 0;
		long cntUserActions = 0;
		
		public SessionInformation(final long timestamp, final long duration, final int cntUserActions) {
			this.timestamp = timestamp;
			this.duration = duration;
			this.cntUserActions = cntUserActions;
		}
		
		/**
		 * @return the cntUserActions
		 */
		public final long getCntUserActions() {
			return cntUserActions;
		}

		/**
		 * @param cntUserActions the cntUserActions to set
		 */
		public final void setCntUserActions(long cntUserActions) {
			this.cntUserActions = cntUserActions;
		}

		/**
		 * @return the timestamp
		 */
		public final long getTimestamp() {
			return timestamp;
		}
		/**
		 * @param timestamp the timestamp to set
		 */
		public final void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		/**
		 * @return the duration
		 */
		public final long getDuration() {
			return duration;
		}
		/**
		 * @param duration the duration to set
		 */
		public final void setDuration(long duration) {
			this.duration = duration;
		}
		
		
	}
}
