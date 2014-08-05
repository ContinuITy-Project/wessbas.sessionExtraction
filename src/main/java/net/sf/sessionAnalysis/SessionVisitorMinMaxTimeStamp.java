package net.sf.sessionAnalysis;

import kieker.tools.util.LoggingTimestampConverter;

/**
 * 
 * @author Andre van Hoorn
 *
 */
public class SessionVisitorMinMaxTimeStamp implements ISessionDatVisitor {

	private volatile long minTimeStamp = Long.MAX_VALUE;

	private volatile long maxTimeStamp = Long.MIN_VALUE;
	
	public void handleSession(Session session) {
		if (session.getSessionStartTimeStamp() < this.minTimeStamp) {
			this.minTimeStamp = session.getSessionStartTimeStamp();
		}
		if (session.getSessionEndTimeStamp() > this.maxTimeStamp) {
			this.maxTimeStamp = session.getSessionEndTimeStamp();
		}
	}

	public void handleEOF() {	}
	
	public long getMinTimeStamp() {
		return minTimeStamp;
	}

	public String getMinDateTime() {
		return LoggingTimestampConverter.convertLoggingTimestampLocalTimeZoneString(minTimeStamp);
	}
	
	public long getMaxTimeStamp() {
		return maxTimeStamp;
	}
	
	public String getMaxDateTime() {
		return LoggingTimestampConverter.convertLoggingTimestampLocalTimeZoneString(maxTimeStamp);
	}

}
