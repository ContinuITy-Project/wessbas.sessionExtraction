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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
class SessionDatReader {
	private final String filename;
	private final Collection<ISessionDatVisitor> visitors = new LinkedList<ISessionDatVisitor>();
	// TODO: Remove after Hack removed:
	private boolean isPrintedHackNote = false;

	public SessionDatReader(final String filename) {
		this.filename = filename;
	}

	public void registerVisitor(ISessionDatVisitor visitor) {
		this.visitors.add(visitor);
	}

	public void read() {
		FileInputStream fis;
		BufferedReader reader = null;

		try {
			fis = new FileInputStream(filename);
			reader = new BufferedReader(new InputStreamReader(fis));

			String line;
			for (line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] sessonInfoSplit = line.split(";");
				String sessionId = sessonInfoSplit[0];
				List<UserAction> userActions = new LinkedList<UserAction>();

				long minTimeStamp = Long.MAX_VALUE;
				long maxTimeStamp = Long.MIN_VALUE;
				
				// Now extract information about the actions
				/* skip first field, which contains the session id */
				for (int i = 1; i < sessonInfoSplit.length; i++) {
					String actionInfo = sessonInfoSplit[i];
					String[] actionInfoSplit = actionInfo.split(":");
					final String invokedAction = actionInfoSplit[0].replaceAll(
							"\"", "");
					final long actionStartTime = Long
							.parseLong(actionInfoSplit[1]);
					final long actionEndTime = Long
							.parseLong(actionInfoSplit[2]);
					final String queryString = actionInfoSplit[8];
					
					final UserAction userAction = new UserAction(actionStartTime, actionEndTime, invokedAction, queryString);
					userActions.add(userAction);
					if (actionStartTime < minTimeStamp) {
						minTimeStamp = actionStartTime;
					}
					if (actionEndTime > maxTimeStamp) {
						maxTimeStamp = actionEndTime;
					}
				}
							
				/* HACK TO GREP STEADY-STATE! */
				if (! isPrintedHackNote ) {
					isPrintedHackNote = true;
					System.err.println("CAUTION: We have included a hack to grep the steady state. Chose code below to analyze full data.");
				}
				long startTime = 1434397382*1000000000L;
				  long endTime = 1434398102*1000000000L;  
			    if (userActions.get(0).getStartTime() > startTime && userActions.get( userActions.size() -1 ).getStartTime() < endTime) { 
				   Session session = new Session(sessionId, userActions, minTimeStamp, maxTimeStamp);
				   this.notifyVisitorsAboutNewSession(session);
			    }		
			    
//			    Session session = new Session(sessionId, userActions, minTimeStamp, maxTimeStamp);
//				   this.notifyVisitorsAboutNewSession(session);
			}
			this.notifyVisitorsAboutEOF();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyVisitorsAboutNewSession(Session session) {
		for (ISessionDatVisitor visitor : this.visitors) {
			visitor.handleSession(session);
		}
	}

	private void notifyVisitorsAboutEOF() {
		for (ISessionDatVisitor visitor : this.visitors) {
			visitor.handleEOF();
		}
	}
}
