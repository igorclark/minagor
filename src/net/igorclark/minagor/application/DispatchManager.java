package net.igorclark.minagor.application;

import com.virtunity.asyncfcgi.common.FCGIRequest;

public class DispatchManager {
	/* Ooh. Yes. It's a singleton. */
	private static DispatchManager	_defaultDispatchManager;

	public static DispatchManager defaultDispatchManager() {
		if(null == _defaultDispatchManager) {
			_defaultDispatchManager = new DispatchManager();
		}
		return _defaultDispatchManager;
	}
	
	private DispatchManager() {
	}
	
	/**
	 * Work out which dispatcher class to use based on the request.
	 * @param fcgiRequest
	 * @return Dispatcher
	 */
	public Dispatcher getDispatcher(FCGIRequest fcgiRequest) {
		DefaultDispatcher	dispatcher = new DefaultDispatcher();
		dispatcher.setFcgiRequest(fcgiRequest);
		return dispatcher;
	}
}
