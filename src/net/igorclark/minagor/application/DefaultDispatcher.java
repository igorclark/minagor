package net.igorclark.minagor.application;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.virtunity.asyncfcgi.common.FCGIRequest;

public class DefaultDispatcher implements Dispatcher {
	
	private FCGIRequest							_fcgiRequest;
	private Map<String, Map<String, String>>	_requestParameters;
	private	Session								_session;
	
	public void setFcgiRequest(FCGIRequest fcgiRequest) {
		_fcgiRequest = fcgiRequest;
	}
	public void setRequestParameters(Map<String, Map<String, String>> requestParameters) {
		_requestParameters = requestParameters;
	}
	public void setSession(Session session) {
		_session = session;
	}
	
	public byte[] dispatch() {
       StringBuffer	msg			= new StringBuffer();

       String			cookie		= 	SessionStore.SESSION_IDENTIFIER_NAME + "=" + _session.id(); // +
		//"; path=/" +
		//"domain=" + _fcgiRequest.getParams().get("HTTP_HOST");
       
       Hashtable<String, String>		headers		= new Hashtable<String, String>();
       headers.put("Set-Cookie",	cookie);
       headers.put("X-Powered-By",	"MinaGor/0.01");
       headers.put("Content-Type",	"text/html");
       
       String			httpStatus	= "HTTP/1.0 200 OK\r\n";

       msg.append(httpStatus);
       
       for(String s : headers.keySet()) {
    	   msg.append(s + ": " + headers.get(s) + "\r\n");
       }
       
       msg.append("\r\n");

       msg.append("<pre>Cookie: " + cookie + "\r\n");
       msg.append("Application count: " + Application.defaultApplication().count() + "\r\n");
       Integer count = (Integer)_session.get("count");
       if(null == count) {
    	   count = new Integer(0);
       }
       count = count + new Integer(1);
       _session.put("count", count);
       _session.put("igor", "starting to get it");
       _session.remove("igor");
       _session.save();
       
       msg.append("Session count: " + ((Integer)_session.get("count")).intValue() + "\r\n");
       msg.append("<hr>\r\n");
       Map<String, String> params = _fcgiRequest.getParams();
       for(Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {  
    	   Map.Entry<String, String>	entry	= (Map.Entry<String, String>)iterator.next(); 
           String		key		= (String)entry.getKey();
           String		value	= (String)entry.getValue();
           msg.append(key + ": " + value + "\r\n");
       }
       msg.append("<hr>\r\n");
       params	= this._requestParameters.get(params.get(EnvConstants.REQUEST_METHOD));
       if(null != params) {
	       for(Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {  
	           Map.Entry<String, String>	entry	= (Map.Entry<String, String>)iterator.next(); 
	           String		key		= (String)entry.getKey();
	           String		value	= (String)entry.getValue();
	           msg.append(key + ": " + value + "\r\n");
	       }
       }
       return msg.toString().getBytes();
	}
	
	public byte[] getStdErr() {
		return new byte[0];
	}
	
}
