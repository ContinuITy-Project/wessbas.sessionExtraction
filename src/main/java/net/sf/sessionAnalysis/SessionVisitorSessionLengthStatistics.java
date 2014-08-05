package net.sf.sessionAnalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 * Computes a histogram of the lengths of the sessions passed to {@link #handleSession(Session)}.
 * 
 * @author Andre van Hoorn
 * 
 */
// TODO: Compute measures once handleEOF called and return precomputed values
public class SessionVisitorSessionLengthStatistics implements
		ISessionDatVisitor {
	private final TreeMap<Integer, AtomicInteger> sessionLengthHistogram = new TreeMap<Integer, AtomicInteger>();

	public void handleSession(Session session) {
		final int curLength = session.getSessionLengthNumActions();
		AtomicInteger curFrequency = sessionLengthHistogram.get(curLength);
		if (curFrequency == null) { // session length not observed before
			curFrequency = new AtomicInteger(0);
			sessionLengthHistogram.put(curLength, curFrequency);
		}
		curFrequency.incrementAndGet();
	}

	public void handleEOF() {
		// nothing to do on termination
	}

	/**
	 * The returned TreeMap contains the observed session lengths as keys
	 * and their absolute frequencies values.
	 */
	public TreeMap<Integer, AtomicInteger> getSessionLengthHistogram() {
		return sessionLengthHistogram;
	}
	
	public double computeSessionLengthMean() {
		double[] lengths = computeLengthVector();
		Mean meanObj = new Mean();
		return meanObj.evaluate(lengths);
	}

	public double computeSessionLengthStdDev() {
		double[] lengths = computeLengthVector();
		StandardDeviation stdDevObj = new StandardDeviation();
		return stdDevObj.evaluate(lengths);
	}
	
	/**
	 *  Transforms the aggregated length information in {@link #sessionLengthHistogram} 
	 *  into a vector.   
	 */
	public double[] computeLengthVector() {
		List<Double> lengths = new LinkedList<Double>();
		for (Entry<Integer, AtomicInteger> entry : this.sessionLengthHistogram.entrySet()) {
			for (int i = 0; i < entry.getValue().get(); i++) {
				lengths.add((double) entry.getKey());
			}
		}
		return 	ArrayUtils.toPrimitive(lengths.toArray(new Double[] {}));
	}
}