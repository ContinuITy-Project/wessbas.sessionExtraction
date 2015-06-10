package net.sf.sessionAnalysis;

public class UserAction {
	private final long startTime;
	private final long endTime;
	private final String actionName;
	private final String queryString;
	
	public UserAction(long startTime, long endTime, String actionName, String queryString) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.actionName = actionName;
		this.queryString = queryString;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public String getActionName() {
		return actionName;
	}

	public final String getQueryString() {
		return queryString;
	}
		
}
