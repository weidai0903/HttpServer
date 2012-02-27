package edu.upenn.cis.cis555.webserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Wei Dai
 * @version 2.9
 *
 */
public class ServletResponse implements HttpServletResponse {
	HashSet<Cookie> cookies=new HashSet<Cookie>();
	HashMap<String,ArrayList<String>> headers=new HashMap<String,ArrayList<String>>();
	int statusCode=HttpServletResponse.SC_OK;
	String characterEncoding="ISO-8859-1";
	boolean committed=false;
	int bufferSize=-1;
	boolean bodyContentWritten=false;
	BufferedOutputStream bos;
	OutputStream output;
	Locale loc=null;
	boolean sendError=false;
	boolean flushed=false;
	PrintWriter pw;
	public boolean getCommitted(){
		return committed;
	}
	public int getStatusCode(){
		return statusCode;
	}
	public HashMap<String,ArrayList<String>> getHeaders(){
		return headers;
	}
	ServletResponse(){
		
	}
	public BufferedOutputStream getBOS(){
		return bos;
	}
	/**
	 * Adds the specified cookie to the response. This method can be called multiple times to set more than one cookie. 
	 */
	public void addCookie(Cookie arg0) {
		cookies.add(arg0);
	}
	/**
	 * Returns a boolean indicating whether the named response header has already been set.
	 */
	public boolean containsHeader(String arg0) {
		return headers.containsKey(arg0);
	}
	/**
	 * session-in-url not required
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}
	/**
	 * not required
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}
	/**
	 * Deprecated
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}
	/**
	 * Deprecated
	 */
	public String encodeRedirectUrl(String arg0) {
		return arg0;
	}
	/**
	 * Sends an error response to the client using the specified status code and clearing the buffer. 
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		if(!committed){
			statusCode=arg0;
			sendError=true;
			committed=true;
			String errorMessage="<html><h>"+arg1+"<h><html>";
			String status="HTTP/1.1 "+statusCode+" "+arg1+"\r\n\r\n";
			PrintStream ps=new PrintStream(output);
			ps.print(status);
			ps.print(errorMessage);
			ps.flush();
			//bos=new BufferedOutputStream(output);
			pw=new PrintWriter(bos);
		}
		else {
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Sends an error response to the client using the specified status.
	 */
	public void sendError(int arg0) throws IOException {
		if(!committed){
			statusCode=arg0;
			String status="HTTP/1.1 "+statusCode;
			PrintStream ps=new PrintStream(output);
			ps.print(status);
			ps.flush();
			pw=new PrintWriter(bos);
			sendError=true;
			committed=true;
		}
		else {
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Sends a temporary redirect response to the client using the specified redirect location URL
	 */
	public void sendRedirect(String arg0) throws IOException {
		if(!committed){
			statusCode=302;
			String status="HTTP/1.1 "+statusCode+" Found\r\n";
			String header="Location: "+arg0+"\r\n";
			PrintStream ps=new PrintStream(output);
			ps.print(status);
			ps.print(header);
			ps.flush();
			committed=true;
		}else{
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Sets a response header with the given name and date-value.
	 */
	public void setDateHeader(String arg0, long arg1) {
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Date date=new Date(arg1);
		ArrayList<String> al=new ArrayList<String>();
		al.add(dateFormat.format(date));
		headers.put(arg0, al);
	}
	/**
	 * Adds a response header with the given name and date-value
	 */
	public void addDateHeader(String arg0, long arg1) {
		if(headers.containsKey(arg0)){
			DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			Date date=new Date(arg1);
			ArrayList<String> dates=headers.get(arg0);
			dates.add(dateFormat.format(date));
		}else{
			setDateHeader(arg0,arg1);
		}
		
	}
	/**
	 * Sets a response header with the given name and value.
	 */
	public void setHeader(String arg0, String arg1) {
		ArrayList<String> al=new ArrayList<String>();
		al.add(arg1);
		headers.put(arg0, al);
	}
	/**
	 * Adds a response header with the given name and value. 
	 */
	public void addHeader(String arg0, String arg1) {
		if(headers.containsKey(arg0)){
			ArrayList<String> dates=headers.get(arg0);
			dates.add(arg1);
		}else{
			setHeader(arg0,arg1);
		}
	}
	/**
	 * Sets a response header with the given name and integer value. 
	 */
	public void setIntHeader(String arg0, int arg1) {
		ArrayList<String> al=new ArrayList<String>();
		al.add(Integer.toString(arg1));
		headers.put(arg0, al);
	}
	/**
	 * Adds a response header with the given name and integer value. 
	 */
	public void addIntHeader(String arg0, int arg1) {
		if(headers.containsKey(arg0)){
			ArrayList<String> dates=headers.get(arg0);
			dates.add(Integer.toString(arg1));
		}else{
			setIntHeader(arg0,arg1);
		}
	}
	/**
	 * Sets the status code for this response. 
	 */
	public void setStatus(int arg0) {
		statusCode=arg0;
	}
	/**
	 * Deprecated
	 */
	public void setStatus(int arg0, String arg1) {
	}

	/**
	 * If no character encoding has been specified, ISO-8859-1 is returned. 
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}
	/**
	 * should return “text/html” by default, and the results of setContentType if it was previously called.
	 */
	public String getContentType() {
		if(headers.containsKey("Content-Type")){
			ArrayList<String> al=headers.get("Content-Type");
			return (String)al.get(0);
		}else return "text/html";
	}
	/**
	 * return null
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}
	/**
	 * Returns a PrintWriter object that can send character text to the client.
	 */
	public PrintWriter getWriter() throws IOException {
		if(bufferSize==-1){
			bufferSize=1024;
			bos=new BufferedOutputStream(output,bufferSize);
		}else{
			bos=new BufferedOutputStream(output,bufferSize);
		}
		pw=new PrintWriter(bos,false);
		return pw;
	}

	public void setOutPutStream(OutputStream output){
		this.output=output;
	}
	/**
	 * Sets the character encoding (MIME charset) of the response being sent to the client, for example, to UTF-8.
	 */
	public void setCharacterEncoding(String arg0) {
		characterEncoding=arg0;
	}
	/**
	 * Sets the length of the content body in the response In HTTP servlets, this method sets the HTTP Content-Length header. 
	 */
	public void setContentLength(int arg0) {
		setIntHeader("Content-Length",arg0);
	}
	/**
	 * Sets the content type of the response being sent to the client, if the response has not been committed yet.
	 */
	public void setContentType(String arg0) {
		setHeader("Content-Type",arg0);
	}
	/**
	 * Sets the preferred buffer size for the body of the response.
	 */
	public void setBufferSize(int arg0) {
		if(!bodyContentWritten&&!committed)
			bufferSize=arg0;
		else{
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Returns the actual buffer size used for the response. If no buffering is used, this method returns 0. 
	 */
	public int getBufferSize() {
		if(bufferSize==-1) return 0;
		else return bufferSize;
	}
	/**
	 * Forces any content in the buffer to be written to the client.
	 */
	public void flushBuffer() throws IOException {
		if(!committed){
			String status="HTTP/1.1 "+statusCode+" OK\r\n";
			StringBuilder sb=new StringBuilder();
			Set<String> headersKey=headers.keySet();
			Iterator<String> iter=headersKey.iterator();
			
			while(iter.hasNext()){
				String nextKey=iter.next();
				sb.append(nextKey);
				sb.append(": ");
				ArrayList<String> values=headers.get(nextKey);
				for(int j=0;j<values.size();j++){
					sb.append(values.get(j));
					if(j!=values.size()-1) sb.append("; ");
				}
				sb.append("\r\n");
			}
			Iterator<Cookie> cookieIter=cookies.iterator();
			while(cookieIter.hasNext()){
				DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
				Cookie c=cookieIter.next();
				if(c.getMaxAge()>=0){
					Date nowDate=new Date();
					Date expireDate=new Date(nowDate.getTime()+c.getMaxAge());
					String expires=dateFormat.format(expireDate);
					sb.append("Set-Cookie: "+c.getName()+"="+c.getValue()+"; "+"Expires="+expires+"\r\n");
				}
				else{
					sb.append("Set-Cookie: "+c.getName()+"="+c.getValue()+"\r\n");
				}
			}
			sb.append("\r\n");
			PrintStream ps=new PrintStream(output);
			ps.print(status);
			ps.print(sb.toString());
			ps.flush();
			committed=true;
			
		}
		pw.flush();
		flushed=true;
		bodyContentWritten=true;
	}
	/**
	 * Clears the content of the underlying buffer in the response without clearing headers or status code.
	 */
	public void resetBuffer() {
		if(!committed)
			bos=new BufferedOutputStream(output);
		else {
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Returns a boolean indicating if the response has been committed. A committed response has already had its status code and headers written. 
	 */
	public boolean isCommitted() {
		return committed;
	}
	/**
	 * Clears any data that exists in the buffer as well as the status code and headers. 	
	 */
	public void reset() {
		if(!committed){
			bos=new BufferedOutputStream(output);
			statusCode=HttpServletResponse.SC_OK;
			headers.clear();
		}
		else {
			HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
			HttpServer.errorLog.append("<p>"+"Servlet Operation Error: Already committed!"+"</p>");
			throw new IllegalStateException();
		}
	}
	/**
	 * Sets the locale of the response, if the response has not been committed yet.
	 */
	public void setLocale(Locale arg0) {
		loc=arg0;
	}
	/**
	 * Returns the locale specified for this response using the setLocale(java.util.Locale) method.
	 */
	public Locale getLocale() {
		return loc;
	}
}
