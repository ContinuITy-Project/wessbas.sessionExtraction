package net.sf.sessionAnalysis;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
public class SessionAnalyzer {
	// private static final String INPUT_SESSIONS_DAT_FN =
	// "../../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat";
	static final String INPUT_SESSIONS_DAT_FN = "../net.sf.markov4jmeter.behaviormodelextractor/examples/menasce/input/generated_session_logs.dat";

	public static void main(String[] args) {

		final SessionDatReader sessionDatReader = new SessionDatReader(INPUT_SESSIONS_DAT_FN);

		final SessionVisitorSessionLengthStatistics sessionVisitorSessionLengthStatistics =
				new SessionVisitorSessionLengthStatistics();
		sessionDatReader.registerVisitor(sessionVisitorSessionLengthStatistics);

		final SessionVisitorMinMaxTimeStamp sessionVisitorMinMaxTimeStamp = new SessionVisitorMinMaxTimeStamp();
		sessionDatReader.registerVisitor(sessionVisitorMinMaxTimeStamp);
		
		final SessionVisitorArrivalAndCompletionRate sessionVisitorArrivalAndCompletionRate =
				new SessionVisitorArrivalAndCompletionRate(1, TimeUnit.MINUTES); 
		sessionDatReader.registerVisitor(sessionVisitorArrivalAndCompletionRate);
		
		sessionDatReader.read();

		/*
		 * Session length histogram. Results can be compared analysis on raw
		 * data: cat
		 * ../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-
		 * UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat |
		 * awk -F ";" '{print NF-1}' | sort -n | uniq -c | wc -l
		 */
		System.out.println("Num sessions: "+ sessionVisitorArrivalAndCompletionRate.getCompletionTimestamps().length);
		System.out.println("Length histogram: " + sessionVisitorSessionLengthStatistics.getSessionLengthHistogram());
		System.out.println("Length vector: " + ArrayUtils.toString(sessionVisitorSessionLengthStatistics.computeLengthVector()));
		System.out.println("Mean length (# user actions): " + sessionVisitorSessionLengthStatistics.computeSessionLengthMean());
		System.out.println("Standard dev (# user actions): " + sessionVisitorSessionLengthStatistics.computeSessionLengthStdDev());
		System.out.println("Timespan (nanos since epoche): " + sessionVisitorMinMaxTimeStamp.getMinTimeStamp() + " - " + sessionVisitorMinMaxTimeStamp.getMaxTimeStamp());
		System.out.println("Timespan (local date/time): " + sessionVisitorMinMaxTimeStamp.getMinDateTime() + " - " + sessionVisitorMinMaxTimeStamp.getMaxDateTime());
		System.out.println("Arrival rates: " + ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getArrivalRates()));
		System.out.println("Completion rates: " + ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getCompletionRates()));
		System.out.println("Concurrent number of sessions over time" + sessionVisitorArrivalAndCompletionRate.getNumConcurrentSessionsOverTime());
		System.out.println("Max number of sessions per time interval: " + ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getMaxNumSessionsPerInterval()));
	}
}
