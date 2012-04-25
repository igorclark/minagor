package net.igorclark.minagor.application;

import java.io.IOException;

public class Application {
	
	private static Application _defaultApplication;
	
	/**
	 * Singleton pattern returning single Application object
	 * @return
	 */
	public static Application defaultApplication() {
		if(null == _defaultApplication) {
			_defaultApplication = new Application();
		}
		return _defaultApplication;
	}
	
	private	Integer	_count;
	public int count() {
		if(null == _count) {
			_count = new Integer(0);
		}
		_count =  _count + new Integer(1);
		return _count.intValue();
	}
	
	private MemcachedSessionStore _sessionStore;
	public MemcachedSessionStore sessionStore() {
		if(null == _sessionStore) {
			try {
				_sessionStore = new MemcachedSessionStore();
			}
			catch(IOException i) {
				System.out.println("Couldn't create MemcachedSessionStore. Exiting.");
				System.exit(1);
			}
		}
		return _sessionStore;
	}
	
	private Application() {
		System.out.println("Constructing Application " + this.toString());
	}

}
