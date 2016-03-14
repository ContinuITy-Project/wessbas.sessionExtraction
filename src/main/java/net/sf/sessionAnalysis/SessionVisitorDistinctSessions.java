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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * 
 * @author Andre van Hoorn
 *
 */
public class SessionVisitorDistinctSessions implements ISessionDatVisitor {

	private final TreeMap<Integer,List<String[]>> distinctSessions = new TreeMap<Integer,List<String[]>>();
	private volatile int numDistinctSessions = 0;
	
	public void handleSession(Session session) {
		final int numActions = session.getSessionLengthNumActions();
		String[] actionSequence = new String[numActions];
		int curIdx = 0;
		for (UserAction a : session.getUserActions()) {
			actionSequence[curIdx] = a.getActionName();
			curIdx++;
		}
		List<String[]> sessionsWithLength = distinctSessions.get(numActions);
		if (sessionsWithLength == null) {
			// First session of this length
			sessionsWithLength = new LinkedList<String[]>();
			sessionsWithLength.add(actionSequence);
			distinctSessions.put(numActions, sessionsWithLength);
			numDistinctSessions++;
			return;
		}
		
		// Search whether session with this structure exists
		for (String[] otherSequence : sessionsWithLength) {
			if (Arrays.equals(actionSequence, otherSequence)) {
				// match
				return;
			}
		}
		// Not found -> Add
		numDistinctSessions++;
		sessionsWithLength.add(actionSequence);
	}

	public void handleEOF() {
		// No need to do anything
	}

	public int numDistinctSessions() {
		return this.numDistinctSessions;
	}
	
	public void writeDistinctSessions(final String outputDir) throws IOException  {
		FileWriter fw = new FileWriter(outputDir + "/" + this.getClass().getSimpleName()+"-distinctSessions.csv");
        BufferedWriter writer = new BufferedWriter(fw);
        
        writer.write("length;actions");
        writer.newLine();
        for (Entry<Integer, List<String[]>> sessionsWithSameLength: this.distinctSessions.entrySet()) {
        	int length = sessionsWithSameLength.getKey();
        	for (String[] session : sessionsWithSameLength.getValue()) {
            	writer.write(length + ";" + Arrays.toString(session));
            	writer.newLine();
        	}
        }
        
        writer.close();
        fw.close();
	}
}
