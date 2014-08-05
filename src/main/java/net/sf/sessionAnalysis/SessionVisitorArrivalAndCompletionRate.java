package net.sf.sessionAnalysis;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

	private volatile long minTimeStampNanos = Long.MAX_VALUE;
	private volatile long maxTimeStampNanos = Long.MIN_VALUE;
	private final List<Long> arrivalTimeStamps = new LinkedList<Long>();
	private final List<Long> completionTimeStamps = new LinkedList<Long>();
	
	private volatile long[] arrivalTimeStampsSorted = null;
	private volatile long[] completionTimeStampsSorted = null;
	
	private volatile int[] arrivalRates = null;
	private volatile int[] completionRates = null;
	private volatile int[] numConcurrentSessions = null;

	
	public SessionVisitorArrivalAndCompletionRate(final int resolutionValue, final TimeUnit resolutionTimeUnit) {
		this.resolutionValueOriginal = resolutionValue;
		this.resolutionTimeUnitOriginal = resolutionTimeUnit;
		this.resolutionValueNanos = TimeUnit.NANOSECONDS.convert(this.resolutionValueOriginal, this.resolutionTimeUnitOriginal);
	}
	
	public void handleSession(Session session) {
		this.arrivalTimeStamps.add(session.getSessionStartTimeStamp());
		this.completionTimeStamps.add(session.getSessionEndTimeStamp());
		if (session.getSessionStartTimeStamp() < this.minTimeStampNanos) {
			this.minTimeStampNanos = session.getSessionStartTimeStamp();
		}
		if (session.getSessionEndTimeStamp() > this.maxTimeStampNanos) {
			this.maxTimeStampNanos = session.getSessionEndTimeStamp();
		}
	}

	public void handleEOF() {
		arrivalTimeStampsSorted = ArrayUtils.toPrimitive(arrivalTimeStamps.toArray(new Long[]{}));
		Arrays.sort(arrivalTimeStampsSorted);
		completionTimeStampsSorted = ArrayUtils.toPrimitive(completionTimeStamps.toArray(new Long[]{}));
		Arrays.sort(completionTimeStampsSorted);
		this.computeRates();
	}

	// TODO: We'll have to find a solution for session that start and end in the same time bucket
	//       It makes sense to compute a metric like min/max/median number of sessions per bucket
	//       based on the sequence of arrival/completions events
	private void computeRates()  {
		final long durationNanos = this.maxTimeStampNanos-this.minTimeStampNanos;
		final int numBuckets = (int) Math.ceil((double)durationNanos / this.resolutionValueNanos);
		
		this.arrivalRates = new int[numBuckets];
		this.completionRates = new int[numBuckets];
		this.numConcurrentSessions = new int[numBuckets];
		
		for (long arrivalTimeStamp : this.arrivalTimeStamps) {
			final int arrivalTimeStampBucket = (int) ((arrivalTimeStamp - this.minTimeStampNanos) / this.resolutionValueNanos);
			arrivalRates[arrivalTimeStampBucket]++;
		}
		
		for (long completionTimeStamp : this.completionTimeStamps) {
			final int completionTimeStampBucket = (int) ((completionTimeStamp - this.minTimeStampNanos) / this.resolutionValueNanos);
			completionRates[completionTimeStampBucket]++;
		}

		// This doesn't make much sense!
		int curNumSessions = 0;
		for (int i=0; i<this.numConcurrentSessions.length; i++) {
			curNumSessions = curNumSessions - this.completionRates[i] + this.arrivalRates[i];
			this.numConcurrentSessions[i] = curNumSessions;
		}
	}
	
	public int[] getArrivalRates() {
		return this.arrivalRates;
	}

	public int[] getCompletionRates() {
		return this.completionRates;
	}

	public int[] getNumConcurrentSessions() {
		return this.numConcurrentSessions;
	}

	public long[] getArrivalTimeStamps() {
		return ArrayUtils.toPrimitive(arrivalTimeStamps.toArray(new Long[]{}));
	}

	public long[] getCompletionTimeStamps() {
		return ArrayUtils.toPrimitive(completionTimeStamps.toArray(new Long[]{}));
	}
}
