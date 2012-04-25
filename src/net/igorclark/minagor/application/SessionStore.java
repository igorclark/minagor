package net.igorclark.minagor.application;

import java.util.Enumeration;
import java.util.Hashtable;

import com.virtunity.asyncfcgi.common.FCGIRequest;

public class SessionStore extends Hashtable<String, Session> {
	
	private	static final long	serialVersionUID			= -8446071803685119139L;
	public	static final String	SESSION_IDENTIFIER_NAME		= "MINAGOR_SESS_ID";

	/**
	 * A SessionStore stores Sessions for an Application.
	 * Currently just uses a hashtable, will be replaced with
	 * database storage or memcached.
	 * @author Igor Clark
	 */
	public	SessionStore() {
		super();
	}
	
	/**
	 * Finds a Session object associated with an FCGIRequest object,
	 * or creates a new one if none exists
	 * @param fcgiRequest
	 * @return
	 */
	public	Session	getSession(FCGIRequest fcgiRequest) {
		String cookie		= fcgiRequest.getParams().get("HTTP_COOKIE");
		String sessionId	= this.getIdFromCookie(cookie);
        return this.getSession(sessionId);
	}
	
	/**
	 * Retrieves an existing Session object from storage, or a new one if null is supplied as the id.
	 * @param id The ID of an existing Session, or null if a new Session is required
	 * @return A new or existing Session object
	 * @throws NullPointerException
	 */
	public	Session getSession(String id) {
		System.out.println(this.getClass().getCanonicalName() + ": asked for Session with id " + id);
		System.out.println("Looking in session list: ");
		Enumeration<String> e = this.keys();
		while(e.hasMoreElements()) {
			System.out.println("  found " + e.nextElement());
		}
		if(null == id || !this.containsKey(id)) {
			Session session = new Session(id);
			id = session.id();
			this.put(id, session);
		}
		return this.get(id);
	}

	/**
	 * Retrieves a session ID from a cookie value.
	 * @param cookie The HTTP cookie as a String.
	 * @return The retrieved session ID as a String, or null if not found.
	 */
    private String getIdFromCookie(String cookie) {
    	if(cookie == null) {
    		return null;
    	}
    	String myCookie = cookie;
    	if(cookie.indexOf("; ") != -1) {
        	String[] cookies = cookie.split("; ");
        	for(int x=0;x<cookies.length;x++) {
        		if(cookies[x].startsWith(SESSION_IDENTIFIER_NAME + "=")) {
        			myCookie = cookies[x];
        			System.out.println("Found it");
        			break;
        		}
        	}
        	if(myCookie.equals(cookie)) {
        		System.out.println("Couldn't find it");
        		return null;
        	}
    	}
		String[] cookieData = myCookie.split("=");
		System.out.println("cookieData.length " + cookieData.length);
		System.out.println("cookieData[0]" + cookieData[0]);
		if(cookieData.length == 2 && cookieData[0].equals(SESSION_IDENTIFIER_NAME)) {
			return cookieData[1].replace("\"", "");
		}
    	return null;
    }

}
