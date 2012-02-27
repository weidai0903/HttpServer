package edu.upenn.cis.cis555.webserver;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * CIS555 HW1MS1
 * @author Wei Dai
 *
 */

public class Response {
	private Request request;
	private OutputStream output;
	private String uri;
	private String httpMethod;
	private String ifModifiedDateStr;
	private ArrayList<String> headers;
	private String protocol;
	private String postBody;
	private Socket socket;
	public static PrintWriter errorWriter=new PrintWriter(Worker.socketOut,false);
	
	public Response(OutputStream output, Request request, Socket socket) {
		this.output = output;
		this.request= request;
		this.socket=socket;
	}
	/**
	 * send static resources
	 * @throws IOException
	 */
	public void sendStaticResource() throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		DateFormat altDateFormat1=new SimpleDateFormat("EEEEEEEEE, dd-MMM-yy HH:mm:ss zzz");
		DateFormat altDateFormat2=new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

		Date nowDate = new Date();
		String badReq = "HTTP/1.1 400 Bad Request\r\n" +
		"Content-Type: text/html\r\n" +
		"Date: "+ dateFormat.format(nowDate)+"\r\n"+
		"Server: HTTP server by weidai\r\n" +
		"Connection: close\r\n" +
		"\r\n" +
		"<h1>Bad Request</h1>";
		String commonOkHeads = "HTTP/1.1 200 OK\r\n" +
		"Content-Type: text/html\r\n" +
		"Date: "+ dateFormat.format(nowDate)+"\r\n"+
		"Server: HTTP server by weidai\r\n"+
		"Connection: close\r\n" +
		"\r\n";
		PrintStream printStream = new PrintStream(output);

		FileInputStream fis = null;
		//get all the information from request
    	uri= request.getUri();
    	httpMethod=request.httpMethod();
    	ifModifiedDateStr=request.getIfModifiedDate();
    	headers=request.getHeaders();
    	protocol=request.getProtocol();
    	postBody=request.getPostBody();
    	
    	
		try {
			if(httpMethod!=null){
				//handle HEAD and GET
			  if(httpMethod.equals("POST")||httpMethod.equals("HEAD")||httpMethod.equals("GET")){
				if(uri!=null){
					ServletContainer sc=new ServletContainer(HttpServer.XMLFilePath,output,headers);
					//String servletName=uri.substring(1);
					
					HashMap<String,String> urls=ServletContainer.getUrls();
					Set<String> s=urls.keySet();
					Iterator<String> iter=s.iterator();
					while(iter.hasNext()){
						String url=iter.next().toString();
						Pattern p=Pattern.compile(".*/\\*");
						Matcher m=p.matcher(url);
						Pattern p2=Pattern.compile("\\*\\.");
						Matcher m2=p2.matcher(url);
						if(m.matches()){
							Pattern urlPattern=Pattern.compile("("+url+"/.*)|("+url+")");
							Matcher urlMatcher=urlPattern.matcher(uri);
							if(urlMatcher.matches()){
								String servletName=urls.get(url);
								//printStream.print(commonOkHeads);
								if(!sc.runServlets(servletName,httpMethod,uri,protocol,postBody)){
									printStream.print(badReq);
								}
								return;
							}
						}else if(m2.lookingAt()){
							Pattern urlPattern=Pattern.compile(".+\\."+url.substring(m2.end()));
							Matcher urlMatcher=urlPattern.matcher(uri);
							if(urlMatcher.matches()){
								String servletName=urls.get(url);
								//printStream.print(commonOkHeads);
								if(!sc.runServlets(servletName,httpMethod,uri,protocol,postBody)){
									printStream.print(badReq);
								}
								return;
							}
						}
						else{
							Pattern urlPattern=Pattern.compile(url);
							Matcher urlMatcher=urlPattern.matcher(uri);
							if(urlMatcher.matches()){
								String servletName=urls.get(url);
								//printStream.print(commonOkHeads);
								if(!sc.runServlets(servletName,httpMethod,uri,protocol,postBody)){
									printStream.print(badReq);
								}
								return;
							}	
						}
					}
//					if(sc.servletExist(servletName)){
//						printStream.print(commonOkHeads);
//						sc.runServlets(servletName,httpMethod);
//						return;
//					}
					//handle shutdown
					if(uri.equals("/shutdown")){
						printStream.print(commonOkHeads);
						if(httpMethod.equals("GET")){
							printStream.print("<h1>Server Terminated</h1>");
							printStream.flush();
							printStream.close();
							//interrupt all the waiting worker threads
							Thread workingThread[]=ThreadPool.getWorkingThreads();
							for(int i=0;i<workingThread.length;i++){
								if(workingThread[i].getState()==Thread.State.WAITING)workingThread[i].interrupt();
							}
							Worker.stopped=true;
							//close the server socket
							HttpServer.getServerSocket().close();
							return;
						}else printStream.flush();
					}else if(uri.equals("/errorlog")){
						printStream.print(commonOkHeads);
						printStream.print("<html><head>Error Log</head>");
						//Response.errorWriter.flush();
						printStream.print(HttpServer.errorLog.toString());
						printStream.print("</html>");
						printStream.flush();
					}
					//handle the control panel
					else if(uri.equals("/control")){
						printStream.print(commonOkHeads);
						if(httpMethod.equals("GET")){
							HttpServer.controlMode=true;
							String stringWorkingThreads;
							Thread workingThreads[]=ThreadPool.getWorkingThreads();
							//combine the working threads information into a string
							stringWorkingThreads=stringWorkingThreads(workingThreads);
							//printStream.print(commonOkHeads);
							printStream.print("<html><head>Wei Dai</head>"+
									"<p>seas login name: weidai</p>"+
									"<p><a href=\"/errorlog\">Error Log</a></p>"+
									"<p>All the worker threads: "+workingThreads.length+"</p>");
							printStream.print("<p>Socket Dispatcher Thread: "+HttpServer.serverTState()+"</p>");
							printStream.print(stringWorkingThreads);
							printStream.print("<p><a href=\"/shutdown\">shutdown</a></p>");
							printStream.print("</html>");
							}
						printStream.flush();
						
					}else{
						//if the uri is a file
					File file = new File(HttpServer.rootDir, request.getUri());
					if (file.exists()) {
						if(file.isFile()){
							long fileLength=file.length();
							byte[] bytes = new byte[1024];
							//figure out what kind the file is
							String contentType=URLConnection.guessContentTypeFromName(file.getName());
							Date ifModifiedDate=null;
							Date lastModifiedDate;
							boolean giveNewest=false;
							//make the file last modified date into Date object
							lastModifiedDate=dateFormat.parse(dateFormat.format(file.lastModified()));

							if(ifModifiedDateStr!=null){
								try{
									//parse the if-modified-date String into Date object
									ifModifiedDate = dateFormat.parse(ifModifiedDateStr);
								}catch(ParseException e){
									try{
										ifModifiedDate = altDateFormat1.parse(ifModifiedDateStr);
									}catch(ParseException e1){
										try{
											ifModifiedDate = altDateFormat2.parse(ifModifiedDateStr);
										}catch(ParseException e2){
											HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
											for(int i=0;i<e2.getStackTrace().length;i++){
												HttpServer.errorLog.append("<p>"+e2.getStackTrace()[i].toString()+"</p>");
											}
											System.out.println("Error,If-modified-date in wrong format");
											ifModifiedDate=null;
										}
									}
									
								}
								
							}
							//compare the last-Modified-date 	with if-modified-date
							if(ifModifiedDate!=null){
								if(ifModifiedDate.before(lastModifiedDate)) giveNewest=true;
							}
							//if if-modified-date is not specified or it is before last-modified, send the content
							if(ifModifiedDate==null||giveNewest){
								String heads= "HTTP/1.1 200 OK\r\n" +
								"Content-Type:"+contentType+"\r\n" +
								"Content-Length:"+fileLength+"\r\n" +
								"Last-Modified:"+dateFormat.format(lastModifiedDate)+"\r\n"+
								"Server: http server by weidai\r\n" +
								"Date:"+dateFormat.format(nowDate)+"\r\n"+
								"Connection: close\r\n" +
								"\r\n";
								printStream.print(heads);
								printStream.flush();
								if(httpMethod.equals("GET")){
									fis = new FileInputStream(file);
									int ch=1;
				    	  			ch = fis.read(bytes, 0, 1024);
									while (ch!=-1) {
										output.write(bytes, 0, ch);
				        	  			ch = fis.read(bytes, 0, 1024);
									}
								}
							}
							else {
								String unmodifiedHeads="HTTP/1.1 304 Not Modified\t\n"+
								"Date:"+dateFormat.format(nowDate)+"\r\n"+
								"Connection: close\r\n" +
								"\r\n";
								printStream.print(unmodifiedHeads);
								printStream.flush();
							}
						}
						//if the uri is a directory, output everything in the directory
						else if(file.isDirectory()){
							//if the path is a directory
							String[] children=file.list();
							printStream.print(commonOkHeads);
							if(httpMethod.equals("GET")){
								printStream.print("<html> <head> Directory </head>");
								for(int i=0;i<children.length;i++){
									printStream.print("<p><a href=\""+request.getUri()+children[i]+"/"+"\">"+children[i]+"</a></p>");
									//System.out.println(children[i]);
								}
								printStream.print("</html>");
							}
							else {}
							printStream.flush();
						}
					}
					else {
						// file not found
						String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
						"Content-Type: text/html\r\n" +
						"Content-Length: 1024\r\n" +
						"Date: "+ dateFormat.format(nowDate)+"\r\n"+
						"Server: HTTP server by weidai\r\n"+
						"Connection: close\r\n" +
						"\r\n" +
						"<h1>File Not Found</h1>";
						output.write(errorMessage.getBytes());
					}
					}
				}
				else{
					output.write(badReq.getBytes());
				}
				
	    	}
			else if(httpMethod.equals("PUT")||httpMethod.equals("DELETE")){
				//POST not implemented
				String errorMessage = "HTTP/1.1 501 Not Implemented\r\n" +
				"Content-Type: text/html\r\n" +
				"Content-Length: 1024\r\n" +
				"Date: "+ dateFormat.format(nowDate)+"\r\n"+
				"Server: HTTP server by weidai\r\n" +
				"Connection: close\r\n" +
				"\r\n" +
				"<h1>501 Not Implemented</h1>";
				output.write(errorMessage.getBytes());
			}
			else {
				output.write(badReq.getBytes());
			}
			}
			else{
				//if httpMethod==null, send Bad Request
				output.write(badReq.getBytes());
			}
    }
    catch (Exception e) {
    	HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
		for(int i=0;i<e.getStackTrace().length;i++){
			HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
		}
       e.printStackTrace();
    }
    finally {
    	output.close();
    	if (fis!=null)
    		fis.close();
    	}
	}
	/**
	 * process the threads information into a String
	 * @param threads
	 * @return String contain all the threads information
	 */
	public String stringWorkingThreads(Thread[] threads){
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<threads.length;i++){
			sb.append("<p>");
			sb.append("No:"+i+" thread id: "+threads[i].getId());
			if(threads[i].getState()==Thread.State.WAITING) sb.append("  waiting</p>");
			else sb.append("  running:"+uri+"</p>");
		}
		return sb.toString();
	}
}