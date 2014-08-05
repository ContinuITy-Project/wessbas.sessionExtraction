package net.sf.sessionAnalysis;

public class UserAction {
	private final long startTime;
	private final long endTime;
	private final String actionName;
	
	public UserAction(long startTime, long endTime, String actionName) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.actionName = actionName;
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
}
