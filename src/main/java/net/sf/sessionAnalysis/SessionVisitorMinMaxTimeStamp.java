/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


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
