package net.igorclark.minagor.application;

import java.util.Map;

import com.virtunity.asyncfcgi.common.FCGIRequest;

public interface Dispatcher {
	public byte[] dispatch();
	public byte[] getStdErr();
	public void setFcgiRequest(FCGIRequest r);
	public void setRequestParameters(Map<String, Map<String, String>> r);
	public void setSession(Session s);
}
