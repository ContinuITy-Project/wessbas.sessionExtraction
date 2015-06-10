package net.sf.sessionAnalysis;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author Christian Voegele
 *
 */
public class SessionVisitorBehaviorMix implements ISessionDatVisitor {

	private HashMap <String, Integer> requestTypeMap = new HashMap<String, Integer>();
	
	public void handleSession(Session session) {		
		UserAction userAction = session.getUserActions().get(0);
		String typeName = "";
		if (userAction.getActionName().equals("login")) {
			Map<String, List<String>> parameterMap;
			try {
				parameterMap = splitQuery(userAction.getQueryString());
				for (String parameterName : parameterMap.keySet()) {
					if (parameterName.equals("type")) {
						typeName = parameterMap.get(parameterName).get(0);
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}			
		}		
		
		if (requestTypeMap.get(typeName) != null) {
			int typeCount = requestTypeMap.get(typeName);
			requestTypeMap.put(typeName, typeCount + 1);
		} else {
			requestTypeMap.put(typeName, 1);
		}	
	}

	public void handleEOF() {	}
	
	public void printRequestTypes() {
		for (String type : requestTypeMap.keySet()) {
			System.out.println(type +  " " + requestTypeMap.get(type));
		}
	}
	
    /**
     * 
     * http://stackoverflow.com/questions/13592236/parse-the-uri-string-into-name-value-collection-in-java.
     * 
     * @param queryString
     * @return Map<String, List<String>>
     * @throws UnsupportedEncodingException
     */
    private static Map<String, List<String>> splitQuery(String queryString) throws UnsupportedEncodingException {
    	  final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();    	  
    	  final String[] pairs = queryString.split("&");    	  
    	  for (String pair : pairs) {
    	    final int idx = pair.indexOf("=");
    	    final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
    	    if (!query_pairs.containsKey(key)) {
    	      query_pairs.put(key, new LinkedList<String>());
    	    }
    	    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
    	    query_pairs.get(key).add(value);
    	  }
    	  
     return query_pairs;
   } 

}
