package net.sf.sessionAnalysis;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
public class SessionAnalyzer {
	static final String INPUT_SESSIONS_DAT_FN = "../../evaluation/20140826-SPECjEnterprise/5-session-analysis/specj_25p_50b_25m_manhattan_3_Cluster/sessions-correctedWithHack-onlyComplete.dat";
	static final String OUTPUT_DIR = "../../evaluation/20140826-SPECjEnterprise/5-session-analysis/specj_25p_50b_25m_manhattan_3_Cluster/";
	
	public static void main(String[] args) throws IOException {

		final SessionDatReader sessionDatReader = new SessionDatReader(INPUT_SESSIONS_DAT_FN);

		final SessionVisitorSessionLengthStatistics sessionVisitorSessionLengthStatistics =
				new SessionVisitorSessionLengthStatistics();
		sessionDatReader.registerVisitor(sessionVisitorSessionLengthStatistics);

		final SessionVisitorMinMaxTimeStamp sessionVisitorMinMaxTimeStamp = new SessionVisitorMinMaxTimeStamp();
		sessionDatReader.registerVisitor(sessionVisitorMinMaxTimeStamp);

		final SessionVisitorArrivalAndCompletionRate sessionVisitorArrivalAndCompletionRate =
				new SessionVisitorArrivalAndCompletionRate(1, TimeUnit.MINUTES);
		sessionDatReader.registerVisitor(sessionVisitorArrivalAndCompletionRate);

		SessionVisitorRequestTypeCounter sessionVisitorRequestTypeCounter = 
				new SessionVisitorRequestTypeCounter();
		sessionDatReader.registerVisitor(sessionVisitorRequestTypeCounter);
		
		final SessionVisitorDistinctSessions sessionVisitorDistinctSessions = 
				new SessionVisitorDistinctSessions();
		sessionDatReader.registerVisitor(sessionVisitorDistinctSessions);
		
		sessionDatReader.read();

		/*
		 * Session length histogram. Results can be compared analysis on raw
		 * data: cat
		 * ../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-
		 * UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat |
		 * awk -F ";" '{print NF-1}' | sort -n | uniq -c | wc -l
		 */
		System.out.println("Num sessions: " + sessionVisitorArrivalAndCompletionRate.getCompletionTimestamps().length);
		System.out.println("Num distinct sessions: " + sessionVisitorDistinctSessions.numDistinctSessions());
		System.out.println("Length histogram: " + sessionVisitorSessionLengthStatistics.getSessionLengthHistogram());
		sessionVisitorSessionLengthStatistics.writeSessionsOverTime(OUTPUT_DIR);
		//System.out.println("Length vector: " + ArrayUtils.toString(sessionVisitorSessionLengthStatistics.computeLengthVector()));
		System.out.println("Mean length (# user actions): " + sessionVisitorSessionLengthStatistics.computeSessionLengthMean());
		System.out.println("Standard dev (# user actions): " + sessionVisitorSessionLengthStatistics.computeSessionLengthStdDev());
		System.out.println("Timespan (nanos since epoche): " + sessionVisitorMinMaxTimeStamp.getMinTimeStamp() + " - "
				+ sessionVisitorMinMaxTimeStamp.getMaxTimeStamp());
		System.out.println("Timespan (local date/time): " + sessionVisitorMinMaxTimeStamp.getMinDateTime() + " - " + sessionVisitorMinMaxTimeStamp.getMaxDateTime());
		{
			System.out.println("Arrival rates: " + ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getArrivalRates()));
			System.out.println("Completion rates: " + ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getCompletionRates()));
			System.out
					.println("Max number of sessions per time interval: "
							+ ArrayUtils.toString(sessionVisitorArrivalAndCompletionRate.getMaxNumSessionsPerInterval()));
			sessionVisitorArrivalAndCompletionRate.writeArrivalCompletionRatesAndMaxNumSessions(OUTPUT_DIR);
		}
		{
			//System.out.println("Concurrent number of sessions over time" + sessionVisitorArrivalAndCompletionRate.getNumConcurrentSessionsOverTime());
			sessionVisitorArrivalAndCompletionRate.writeSessionsOverTime(OUTPUT_DIR);
		}
		{
			sessionVisitorRequestTypeCounter.writeCallFrequencies(OUTPUT_DIR);
		}
		{
			sessionVisitorDistinctSessions.writeDistinctSessions(OUTPUT_DIR);
		}
	}
}
