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
