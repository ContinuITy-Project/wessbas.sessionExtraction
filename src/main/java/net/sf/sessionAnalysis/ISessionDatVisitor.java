package net.sf.sessionAnalysis;

/**
 * 
 * @author Andre van Hoorn
 *
 */
public interface ISessionDatVisitor {
	
	/**
	 * Notification that the given session has been processed.
	 */
	public void handleSession(Session session);

	/**
	 * Notification that all sessions read.
	 */
	public void handleEOF();
}