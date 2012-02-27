package edu.upenn.cis.cis555.webserver;

import javax.servlet.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wei Dai
 */
class Context implements ServletContext {
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	private String realPath;
	
	public Context() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	protected void setPath(String path){
		this.realPath=path;
	}
	/**
	 * Returns the servlet container attribute with the given name
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	/**
	 * Returns an Enumeration containing the attribute names available within this servlet context.
	 */
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	/**
	 *Returns a ServletContext object that corresponds to a specified URL on the server.
	 */
	public ServletContext getContext(String name) {
		return this;
	}
	/**
	 * Returns a String containing the value of the named context-wide initialization parameter
	 */
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	/**
	 * Returns the names of the context's initialization parameters as an Enumeration of String objects
	 */
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	/**
	 * Returns the major version of the Java Servlet API that this servlet container supports.
	 */
	public int getMajorVersion() {
		return 2;
	}
	/**
	 * return null
	 */
	public String getMimeType(String file) {
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	/**
	 * return null
	 */
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}
	/**
	 * Gets the real path corresponding to the given virtual path. 
	 * For example, getRealPath("/foo/bar.html") should return "/a/b/c/foo/bar.html".
	 */
	public String getRealPath(String path) {
		Pattern p=Pattern.compile(".*/");
		Matcher m=p.matcher(realPath);
		int index=0;
		if(m.lookingAt()){
			index=m.end();
			StringBuilder sb=new StringBuilder();
			sb.append(realPath.substring(0, index));
			sb.append("classes");
			sb.append(path);
			return sb.toString();
		}else{
			HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
			HttpServer.errorLog.append("<p>"+"wrong Url format"+"</p>");
			return null;
		}
		
	}
	/**
	 * return null
	 */
	public RequestDispatcher getRequestDispatcher(String name) {
		return null;
	}
	/**
	 * return null
	 */
	public java.net.URL getResource(String path) {
		return null;
	}
	/**
	 * return null
	 */
	public java.io.InputStream getResourceAsStream(String path) {
		return null;
	}
	/**
	 * return null
	 */
	public java.util.Set getResourcePaths(String path) {
		return null;
	}
	/**
	 * Returns the name and version of the servlet container on which the servlet is running.
	 */
	public String getServerInfo() {
		return "HTTP Server by Wei Dai";
	}
	/**
	 * Deprecated return null
	 */
	public Servlet getServlet(String name) {
		return null;
	}
	/**
	 *  Returns the name of this web application corresponding to this ServletContext
	 */
	public String getServletContextName() {
		return "";
	}
	/**
	 * Deprecated return null
	 */
	public Enumeration getServletNames() {
		return null;
	}
	/**
	 * Deprecated return null
	 */
	public Enumeration getServlets() {
		return null;
	}
	/**
	 * return null
	 */
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	/**
	 * return null
	 */
	public void log(String msg) {
		System.err.println(msg);
	}
	/**
	 * return null
	 */
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	/**
	 * Removes the attribute with the given name from the servlet context
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	/**
	 * Binds an object to a given attribute name in this servlet context.
	 */
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
