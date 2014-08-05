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
					final UserAction userAction = new UserAction(actionStartTime, actionEndTime, invokedAction);
					userActions.add(userAction);
					if (actionStartTime < minTimeStamp) {
						minTimeStamp = actionStartTime;
					}
					if (actionEndTime > maxTimeStamp) {
						maxTimeStamp = actionEndTime;
					}
				}

				Session session = new Session(sessionId, userActions, minTimeStamp, maxTimeStamp);
				this.notifyVisitorsAboutNewSession(session);
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
