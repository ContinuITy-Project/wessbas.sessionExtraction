package net.sf.sessionAnalysis;

import java.util.HashMap;


/**
 * 
 * @author Christian Voegele
 *
 */
public class SessionVisitorResponseTimes implements ISessionDatVisitor {

	private HashMap <String, ResponseTimeEntry> responseTimesRequests = new HashMap<String, ResponseTimeEntry>();
	
	public void handleSession(Session session) {
		for (UserAction userAction : session.getUserActions()) {
			String requestName = userAction.getActionName();
			long responseTime =  userAction.getEndTime() - userAction.getStartTime();			
			if (responseTimesRequests.get(requestName) != null) {
				ResponseTimeEntry responseTimeEntry = responseTimesRequests.get(requestName);
				responseTimeEntry.setCountRequests(responseTimeEntry.getCountRequests() +1);
				responseTimeEntry.setSumTime(responseTimeEntry.getSumTime() + responseTime);
			} else {
				responseTimesRequests.put(requestName, new ResponseTimeEntry(responseTime, 1));
			}			
		}
	}

	public void handleEOF() {	}
	
	public void printResponseTimes () {
		for (String request: responseTimesRequests.keySet()) {
			System.out.println(request +  " : " + (responseTimesRequests.get(request).getSumTime() / responseTimesRequests.get(request).getCountRequests()));
		}
	}

	class ResponseTimeEntry {
		
		private long sumTime;
		private int countRequests;
		
		public ResponseTimeEntry (final long sumTime, final int countRequests) {
			this.sumTime = sumTime;
			this.countRequests = countRequests; 
		}
		
		/**
		 * @return the sumTime
		 */
		public final long getSumTime() {
			return sumTime;
		}
		/**
		 * @param sumTime the sumTime to set
		 */
		public final void setSumTime(long sumTime) {
			this.sumTime = sumTime;
		}
		/**
		 * @return the countRequests
		 */
		public final int getCountRequests() {
			return countRequests;
		}
		/**
		 * @param countRequests the countRequests to set
		 */
		public final void setCountRequests(int countRequests) {
			this.countRequests = countRequests;
		}		
		
	}

}
