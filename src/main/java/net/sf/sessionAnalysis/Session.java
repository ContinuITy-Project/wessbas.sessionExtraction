package net.sf.sessionAnalysis;

import java.util.List;

/**
 * 
 * @author Andre van Hoorn
 *
 */
public class Session {
	private final String sessionId;
	private final List<UserAction> userActions;
	private final int sessionLengthNumActions;
	
	private final long sessionStartTimeStamp;
	public long getSessionStartTimeStamp() {
		return sessionStartTimeStamp;
	}

	public long getSessionEndTimeStamp() {
		return sessionEndTimeStamp;
	}

	private final long sessionEndTimeStamp;
	
	public Session(String sessionId, List<UserAction> userActions, long sessionStartTimestamp, long sessionEndTimeStamp) {
		this.sessionId = sessionId;
		this.userActions = userActions;
		this.sessionLengthNumActions = this.userActions.size();
		this.sessionStartTimeStamp = sessionStartTimestamp;
		this.sessionEndTimeStamp = sessionEndTimeStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public List<UserAction> getUserActions() {
		return userActions;
	}
	
	public int getSessionLengthNumActions() {
		return this.sessionLengthNumActions;
	}
	
	public long getSessionLengthNanos() {
		return this.sessionEndTimeStamp-this.sessionStartTimeStamp;
	}
}
