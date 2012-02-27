package edu.upenn.cis.cis555.webserver;

import javax.servlet.*;
import java.util.*;

/**
 * @author Wei Dai
 */
class Config implements ServletConfig {
	private String name;
	private Context context;
	private HashMap<String,String> initParams;
	
	public Config(String name, Context context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}
	/**
	 * Returns a String containing the value of the named initialization parameter, or null if the parameter does not exist. 
	 */
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	/**
	 * Returns the names of the servlet's initialization parameters as an Enumeration of String objects
	 */
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	/**
	 * Returns a reference to the ServletContext in which the caller is executing
	 */
	public ServletContext getServletContext() {
		return context;
	}
	/**
	 * Returns the name of this servlet instance
	 */
	public String getServletName() {
		return name;
	}
	public void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
