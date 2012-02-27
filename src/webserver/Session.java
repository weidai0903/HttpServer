package edu.upenn.cis.cis555.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author Wei Dai
 */
class Session implements HttpSession {
	boolean isNew=true;
	long creationTime;
	long lastAccessedTime;
	String Id;
	Context context;
	int maxInactiveInterval=-1;//in seconds
	private Properties m_props = new Properties();
	private boolean m_valid = true;

	public void setContext(Context context){
		this.context=context;
	}
	Session(){
		Date now=new Date();
		creationTime=now.getTime();
		isNew=true;
	}
	/**
	 * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT. 
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Returns a string containing the unique identifier assigned to this session. 
	 */
	public void setId(String id){
		Id=id;
	}
	public String getId() {
		return Id;
	}
	public void setLastAccessedTime(long time){
		lastAccessedTime=time;
	}
	/**
	 * Returns the last time the client sent a request associated with this session
	 */
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}
	/**
	 * Returns the ServletContext to which this session belongs. 
	 */
	public ServletContext getServletContext() {
		return context;
	}
	/**
	 * Specifies the time, in seconds, between client requests before the servlet container will invalidate this session
	 */
	public void setMaxInactiveInterval(int arg0) {
		maxInactiveInterval=arg0;
	}
	/**
	 * Returns the maximum time interval, in seconds, that the servlet container will keep this session open between client accesses
	 */
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}
	/**
	 * deprecated
	 */
	public HttpSessionContext getSessionContext() {
		return null;
	}
	/**
	 * return the attribute binded with the key
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	/**
	 * Deprecated. replaced by getAttribute
	 */
	public Object getValue(String arg0) {
		return m_props.get(arg0);
	}
	/**
	 *  Returns an Enumeration of String objects containing the names of all the objects bound to this session.
	 */
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}
	/**
	 * Deprecated replaced by getAttributeNames
	 */
	public String[] getValueNames() {
		return null;
	}
	/**
	 * Binds an object to this session, using the name specified.
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}
	/**
	 * Deprecated
	 */
	public void putValue(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/**
	 *   Removes the object bound with the specified name from this session.
	 */
	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}
	/**
	 *Deprecated.this method is replaced by removeAttribute(java.lang.String)
	 */
	public void removeValue(String arg0) {
		m_props.remove(arg0);
	}
	/**
	 * nvalidates this session then unbinds any objects bound to it.
	 */
	public void invalidate() {
		m_valid = false;
		m_props.clear();
	}

	/**
	 * Returns true if the client does not yet know about the session or if the client chooses not to join the session.	
	 */
	public boolean isNew() {
		return isNew;
	}
	public void setNotNew(){
		isNew=false;
	}

	boolean isValid() {
		return m_valid;	
	}
	

}
