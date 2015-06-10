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
	
	private volatile long sessionTimeLength = 0;
	
	private volatile long nbrOfSessions = 0;
	
	
	public void handleSession(Session session) {
		if (session.getSessionStartTimeStamp() < this.minTimeStamp) {
			this.minTimeStamp = session.getSessionStartTimeStamp();
		}
		if (session.getSessionEndTimeStamp() > this.maxTimeStamp) {
			this.maxTimeStamp = session.getSessionEndTimeStamp();
		}
	
		sessionTimeLength += session.getUserActions().get(session.getUserActions().size()-1).getEndTime() - session.getUserActions().get(0).getStartTime();
		nbrOfSessions += 1;
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
	
	public double getAverageSessionTimeLength() {
		return  ((double) sessionTimeLength/nbrOfSessions) / 1000000000;
	}
	
	public String getMaxDateTime() {
		return LoggingTimestampConverter.convertLoggingTimestampLocalTimeZoneString(maxTimeStamp);
	}

}
