package net.igorclark.minagor.application;

import java.io.Serializable;
import java.util.Hashtable;

public class Session extends Hashtable<Object, Object> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private	String	_id;
	public	String	id() {
		return	_id;
	}
	
	public Session(String id) {
		if(null == id) {
			this._id = this.createId();
		}
		else {
			this._id = id;
		}
		System.out.println(this.getClass().getCanonicalName() + ": created with ID " + this._id);
	}
	
	public void save() {
		Application.defaultApplication().sessionStore().saveSession(this);
	}
	
	private String createId() {
		return new Integer(new java.util.Date().hashCode()).toString();
	}
}
