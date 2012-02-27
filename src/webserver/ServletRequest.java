package edu.upenn.cis.cis555.webserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Wei Dai
 * 
 */
class ServletRequest implements HttpServletRequest {
	String sessionId=null;
	ServletResponse response;
	Context context;
	String initSessionTimeout;
	boolean committed=false;
	public void setCommited(){
		committed=true;
	}

	ServletRequest() {
	}
	
	ServletRequest(Session session) {
		m_session = session;
	}
	ServletRequest(HashMap<String,Session> sessions,ServletResponse response,Context context, String sessionTimeout) {
		this.sessions = sessions;
		this.response=response;
		this.context=context;
		this.initSessionTimeout=sessionTimeout;
	}
	
	
	public String getAuthType() {
		return BASIC_AUTH;
	}
	/**
	 * Returns an array containing all of the Cookie objects the client sent with this request. This method returns null if no cookies were sent. 
	 */
	public Cookie[] getCookies() {
		ArrayList<Cookie> al=new ArrayList<Cookie>();
		String cookiesStr=(String)headers.get("Cookie");
		//System.out.println(cookiesStr);
		if(cookiesStr!=null){
			
			String cookieStrs[]=cookiesStr.split(";");
			for(int i=0;i<cookieStrs.length;i++){
				String temp=cookieStrs[i].trim();
				String splitted[]=temp.split("=");
				Cookie c=new Cookie(splitted[0].trim(),splitted[1].trim());
				al.add(c);
				if(splitted[0].trim().equals("sessionId")){
					sessionId= splitted[1].trim();
				}
			}
			int len=al.size();
			Cookie[] cookiesFromRequest=new Cookie[len];
			for(int i=0;i<len;i++) cookiesFromRequest[i]=al.get(i);
			return cookiesFromRequest;
		}
		else return null;
	}
	/**
	 * retrieve session information from cookie
	 */
	public void getSessionId(){
		
		String cookiesStr=(String)headers.get("Cookie");
		//System.out.println(cookiesStr);
		if(cookiesStr!=null){
			String cookieStrs[]=cookiesStr.split(";");
			for(int i=0;i<cookieStrs.length;i++){
				String temp=cookieStrs[i].trim();
				String splitted[]=temp.split("=");
				if(splitted[0].trim().equals("sessionId")){
					sessionId= splitted[1].trim();
					if(sessions.containsKey(sessionId)){
						m_session=sessions.get(sessionId);
						Date nowDate=new Date();
						long lastAccessedTime=m_session.getLastAccessedTime();
						m_session.setNotNew();
						if(m_session.getMaxInactiveInterval()>0){
							if(nowDate.getTime()-lastAccessedTime>m_session.getMaxInactiveInterval()*1000){
								m_session.invalidate();
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Returns the value of the specified request header as a long value that represents a Date object.
	 */
	public long getDateHeader(String arg0) {
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		DateFormat altDateFormat1=new SimpleDateFormat("EEEEEEEEE, dd-MMM-yy HH:mm:ss zzz");
		DateFormat altDateFormat2=new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

		String dateStr=headers.getProperty(arg0);
		if(dateStr==null) return -1;
		Date headDate;
		try{
			headDate = dateFormat.parse(dateStr);
		}catch(ParseException e){
			try{
				headDate = altDateFormat1.parse(dateStr);
			}catch(ParseException e1){
				try{
					headDate = altDateFormat2.parse(dateStr);
				}catch(ParseException e2){
					HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
					HttpServer.errorLog.append("<p>"+"Wrong date format :"+"</p>");
					for(int i=0;i<e2.getStackTrace().length;i++){
						HttpServer.errorLog.append("<p>"+e2.getStackTrace()[i].toString()+"</p>");
					}
					System.out.println("Error,If-modified-date in wrong format");
					e2.printStackTrace();
					throw new IllegalArgumentException();
				}
			}
		}
		return headDate.getTime();
	}

	/**
	 * 
	 * Returns the value of the specified request header as a String.
	 */
	public String getHeader(String arg0) {
		return headers.getProperty(arg0);
	}

	/**
	 * Returns all the values of the specified request header as an Enumeration of String objects. 
	 */
	public Enumeration getHeaders(String arg0) {
		String value=headers.getProperty(arg0);
		if(value!=null){
			String values[]=value.split(",");
			Set<String> valuesSet = new HashSet<String>();
			for(int i=0;i<values.length;i++){
				valuesSet.add(values[i].trim());
			}
			Vector<String> atts = new Vector<String>(valuesSet);
			return atts.elements();
		}else{
			Vector<String> atts = new Vector<String>();
			return atts.elements();
		}
	}
	/**
	 * Returns an enumeration of all the header names this request contains. If the request has no headers, this method returns an empty enumeration. 
	 */
	public Enumeration getHeaderNames() {
		return headers.keys();
	}
	/**
	 * Returns the value of the specified request header as an int. If the request does not have a header of the specified name, this method returns -1. If the header cannot be converted to an integer, this method throws a NumberFormatException. 
	 */
	public int getIntHeader(String arg0) {
		String value=headers.getProperty(arg0);
		if(value==null) return -1;
		try{
			return Integer.parseInt(value);
			
		}catch(NumberFormatException e){
			HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
			for(int i=0;i<e.getStackTrace().length;i++){
				HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
			}
			//e.printStackTrace(Response.errorWriter);
			return -1;
			//throw new NumberFormatException();
		}
	}

	public String getMethod() {
		return m_method;
	}
	/**
	 * Returns any extra path information associated with the URL the client sent when it made this request. 
	 * 
	 */
	public String getPathInfo() {
		Pattern p1=Pattern.compile("/.*?/");
		Matcher m1=p1.matcher(uri);
		int index1;
		int index2;
		if(m1.lookingAt()){
			index1=m1.end();
			String subS=uri.substring(index1);
			Pattern p2=Pattern.compile("[\\w/]*\\?*?");
			Matcher m2=p2.matcher(subS);
			if(m2.lookingAt()){
				index2=m2.end();
				return "/"+subS.substring(0, index2);
			}else return "";
		}
		else return "";
	}
	/**
	 * return null
	 */
	public String getPathTranslated() {
		return null;
	}

	/**
	 * For servlets in the default (root) context, this method returns ""
	 */
	public String getContextPath() {
		return "";
	}

	/**
	 * a String containing the query string or null if the URL contains no query string. 
	 */
	public String getQueryString() {
		Pattern p1=Pattern.compile("\\?.*");
		Matcher m1=p1.matcher(uri);
		if(m1.find()){
			int index1=m1.start();
			int index2=m1.end();
			return uri.substring(index1+1, index2);
		}else{
			return null;
		}
	}

	/**
	 * return null
	 */
	public String getRemoteUser() {
		return null;
	}
	/**
	 * return null
	 */
	public boolean isUserInRole(String arg0) {
		return false;
	}
	/**
	 * return null
	 */
	public Principal getUserPrincipal() {
		return null;
	}
	/**
	 * Returns the session ID specified by the client. 
	 */
	public String getRequestedSessionId() {
		return m_session.getId();
	}

	/**
	 * Returns the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request
	 */
	public String getRequestURI() {
		int index=uri.indexOf('?');
		if(index==-1) index=uri.length();
		return uri.substring(0, index);
	}
	/**
	 * Reconstructs the URL the client used to make the request. The returned URL contains a protocol, server name, port number, and server path, but it does not include query string parameters. 
	 */
	public StringBuffer getRequestURL() {
		StringBuffer sb=new StringBuffer();
		sb.append("http://");
		sb.append(getServerName());
		sb.append(":");
		sb.append(getLocalPort());
		sb.append(getRequestURI());
		return sb;
	}
	/**
	 * Returns the part of this request's URL that calls the servlet. 
	 */
	public String getServletPath() {
		Pattern p1=Pattern.compile("/.*?/");
		Matcher m1=p1.matcher(uri);
		if(m1.lookingAt()){
			int index=m1.end();
			return uri.substring(0, index-1);
		}else{
			return "";
		}
	}
	/**
	 * Returns the current HttpSession associated with this request or, if there is no current session and create is true, returns a new session. 
	 */
	public HttpSession getSession(boolean arg0) {
		getSessionId();
		Date now=new Date();
		if (arg0) {
			if (! hasSession()) {
				m_session=new Session();
				sessionId=Long.toString(createSessionId());
				m_session.setId(sessionId);
				m_session.setContext(context);
				m_session.setLastAccessedTime(now.getTime());
				//System.out.println(initSessionTimeout);
				m_session.setMaxInactiveInterval(Integer.parseInt(initSessionTimeout)*60);
				sessions.put(sessionId, m_session);
				Cookie c= new Cookie("sessionId",sessionId);
				response.addCookie(c);
				System.out.println("new "+sessionId);
			}else{
				m_session.setLastAccessedTime(now.getTime());
			}
			
		} else {
			if (! hasSession()) {
				m_session = null;
			}else{
				m_session.setLastAccessedTime(now.getTime());
			}
			
		}
		return m_session;
	}
	/**
	 *   Returns the current session associated with this request, or if the request does not have a session, creates one.
	 */
	
	public HttpSession getSession() {
		if(!response.committed){
			getSessionId();
			Date now=new Date();
			if(hasSession()){
				m_session.setLastAccessedTime(now.getTime());
				return m_session;
			}
			else{
				m_session=new Session();
				sessionId=Long.toString(createSessionId());
				m_session.setId(sessionId);
				m_session.setContext(context);
				m_session.setLastAccessedTime(now.getTime());
				m_session.setMaxInactiveInterval(Integer.parseInt(initSessionTimeout)*60);
				sessions.put(sessionId, m_session);
				Cookie c= new Cookie("sessionId",sessionId);
				response.addCookie(c);
				return m_session;
			}
		}else{
			throw new IllegalStateException();
		}
	}
	static Random rand=new Random();
	public long createSessionId(){
		return rand.nextLong();
	}
	/**
	 * Checks whether the requested session ID is still valid. 
	 */
	public boolean isRequestedSessionIdValid() {
		if(!sessions.containsKey(sessionId)||sessionId==null||m_session==null||!m_session.isValid()) return false;
		return true;
	}

	/**
	 * Return true 
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	/**
	 * Return false
	 */
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}
	/**
	 * Deprecated
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
	/**
	 * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists. 
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}
	/**
	 * an Enumeration of strings containing the names of the request's attributes
	 */
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}
	/**
	 * should return “ISO-8859-1” by default, and the results of setCharacterEncoding if it was previously called.
	 */
	public String getCharacterEncoding() {
		if(characterEncoding==null) return "ISO-8859-1";
		else return characterEncoding;
	}

	/**
	 * Overrides the name of the character encoding used in the body of this request.
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		characterEncoding=arg0;

	}
	/**
	 * Returns the length, in bytes, of the request body and made available by the input stream, or -1 if the length is not known. 
	 */
	public int getContentLength() {
		if(headers.containsKey("Content-Length")){
			int len=-1;
			try{
				len=Integer.parseInt(headers.getProperty("Content-Length"));
			}catch(NumberFormatException e){
				HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
				for(int i=0;i<e.getStackTrace().length;i++){
					HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
				}
				e.printStackTrace();
			}
			return len;
		}
		else return -1;
	}
	/**
	 * eturns the MIME type of the body of the request, or null if the type is not known.
	 */
	public String getContentType() {
		if(headers.containsKey("Content-Type"))
			return headers.getProperty("Content-Type");
		else return null;
	}
	/**
	 * return null	
	 */
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	/**
	 * a String representing the single value of the parameter
	 */
	public String getParameter(String arg0) {
		if(m_params.containsKey(arg0)){
			//String params[]=m_params.getProperty(arg0).split(",");
			return m_params.getProperty(arg0);
		}else{
			return null;
		}
	}
	/**
	 * Returns an Enumeration of String objects containing the names of the parameters contained in this request.
	 */
	public Enumeration getParameterNames() {
		return m_params.keys();
	}

	/**
	 * an array of String objects containing the parameter's values
	 */
	public String[] getParameterValues(String arg0) {
		if(m_params.containsKey(arg0)){
			System.out.println(m_params);
			String params[]=m_params.getProperty(arg0).split(",");
			return params;
		}else return null;
	}
	/**
	 * an immutable java.util.Map containing parameter names as keys and parameter values as map values.
	 */
	public Map getParameterMap() {
		Map<String,String[]> paramMap=new HashMap<String,String[]>();
		Iterator<Object> iter=m_params.keySet().iterator();
		while(iter.hasNext()){
			String key=(String)iter.next();
			String params[]=m_params.getProperty(key).split(",");
			paramMap.put(key, params);
		}
		return paramMap;
	}
	/**
	 * Returns the name and version of the protocol the request uses in the form protocol/majorVersion.minorVersion, for example, HTTP/1.1
	 */
	public String getProtocol() {
		return protocol;
	}
	/**
	 * return http
	 */
	public String getScheme() {
		return "http";
	}
	/**
	 * Returns the host name of the server to which the request was sent.
	 */
	public String getServerName() {
		return Worker.getServerHostName();
	}
	/**
	 * Returns the port number to which the request was sent.
	 */
	public int getServerPort() {
		return HttpServer.serverPortNum;
	}
	/**
	 * Retrieves the body of the request as character data using a BufferedReader. 
	 */
	public BufferedReader getReader() throws IOException {
		StringReader sr=new StringReader(postBody);
		BufferedReader bf=new BufferedReader(sr);
		return bf;
	}
	/**
	 * Returns the Internet Protocol (IP) address of the client or last proxy that sent the request.
	 */
	public String getRemoteAddr() {
		return IPAddress;
	}
	/**
	 * Returns the fully qualified name of the client or the last proxy that sent the request.
	 */
	public String getRemoteHost() {
		return Worker.getClientHostName();
	}
	/**
	 * Stores an attribute in this request.
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}
	/**
	 * Removes an attribute from this request.
	 */
	public void removeAttribute(String arg0) {
		m_props.remove(arg0);
	}
	/**
	 * Returns the preferred Locale that the client will accept content in
	 */
	public Locale getLocale() {
		return loc;
	}
	public void setLocale(Locale arg0){
		loc=arg0;
	}
	/**
	 * return null
	 */
	public Enumeration getLocales() {
		return null;
	}
	/**
	 * Returns a boolean indicating whether this request was made using a secure channel, such as HTTPS. 
	 */
	public boolean isSecure() {
		return false;
	}
	/**
	 * return null
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}
	/**
	 * Deprecated
	 */
	public String getRealPath(String arg0) {
		return null;
	}
	/**
	 * Returns the Internet Protocol (IP) source port of the client or last proxy that sent the request. 
	 */
	public int getRemotePort() {
		return Worker.getClientPort();
	}
	/**
	 * Returns the host name of the Internet Protocol (IP) interface on which the request was received. 
	 */
	public String getLocalName() {
		return Worker.getServerHostName();
	}
	/**
	 * Returns the Internet Protocol (IP) address of the interface on which the request was received. 
	 */
	public String getLocalAddr() {
		return Worker.getServerIP();
	}
	/**
	 * Returns the Internet Protocol (IP) port number of the interface on which the request was received. 
	 */
	public int getLocalPort() {
		return HttpServer.serverPortNum;
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		m_params.setProperty(key, value);
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
	
	void setProtocol(String arg0){
		this.protocol=arg0;
	}
	void setUri(String arg0){
		this.uri=arg0;
		if(getQueryString()!=null){
			String query[]=getQueryString().split("&");
			for(int i=0;i<query.length;i++){
				String[] nameAndValue=query[i].split("=");
				//System.out.println(i+" "+nameAndValue[0]+" "+nameAndValue[1]);
				this.setParameter(nameAndValue[0].trim(),nameAndValue[1].trim());
			}
		}
	}
	void setHeaders(String arg0,String arg1){
		headers.put(arg0, arg1);
	}
	void setBodyString(String arg0){
		if(arg0!=null){
			this.postBody=arg0;
			String query[]=postBody.split("&");
			for(int i=0;i<query.length;i++){
				String[] nameAndValue=query[i].split("=");
				if(nameAndValue.length==2){
				//System.out.println(i+" "+nameAndValue[0]+" "+nameAndValue[1]);
					this.setParameter(nameAndValue[0].trim(),nameAndValue[1].trim());
				}else{
					HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
					HttpServer.errorLog.append("<p>"+"Postbody parameter in wrong format"+"</p>");
				}
			}
		}
	}
	void setIPAddress(String arg0){
		IPAddress=arg0;
	}
	private String characterEncoding=null;
	private Properties m_params = new Properties();
	private Properties headers = new Properties();
	private Properties m_props = new Properties();
	private Session m_session = null;
	private HashMap<String,Session> sessions;
	private String m_method;
	private String protocol;
	private String uri;
	private String postBody;
	private String IPAddress;
	private Locale loc;
}
