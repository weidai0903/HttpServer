package edu.upenn.cis.cis555.webserver;

import java.io.*;
import java.net.Socket;
/**
 * CIS555 HW1MS1
 * @author Wei Dai(weidai)
 *
 */
public class Worker implements Runnable{
    protected Socket clientSocket = null;
    protected String serverText   = null;
    public static boolean stopped=false;
    private static String serverHostName;
    private static int serverPortNum;
    private static String serverIP;
    private OutputStream output;
    private static String clientIP;
    private static String clientHostName;
    private static int clientPort;
    public static OutputStream socketOut;
    public static String getServerIP(){
    	return serverIP;
    }
    public static int getClientPort(){
    	return clientPort;
    }
    public static String getClientHostName(){
    	return clientHostName;
    }
    public static String getClientIP(){
    	return clientIP;
    }
    public static String getServerHostName(){
    	return serverHostName;
    }
    public static int getServerPortNum(){
    	return serverPortNum;
    }
    public OutputStream getOutputStream(){
    	return output;
    }
    public void run() {
    	while(!stopped){
    		try {
    			clientSocket=ThreadPool.mon.remove();
    	    	if(clientSocket==null) break;
    	    	if(clientSocket.isClosed()) continue;
    			serverHostName=clientSocket.getInetAddress().getHostName();
    			serverPortNum=clientSocket.getPort();
    			clientIP=clientSocket.getLocalAddress().getHostAddress();
    			clientHostName=clientSocket.getLocalAddress().getHostName();
    			clientPort=clientSocket.getLocalPort();
    			serverIP=clientSocket.getInetAddress().getHostAddress();
    			InputStream input  = clientSocket.getInputStream();
    	 		output = clientSocket.getOutputStream();
    	 		socketOut=output;
    			//create the request object to handle the inputStream
    			Request request = new Request(input);
    			request.parse();
    			//create the response object to sent messages to output
    			Response response = new Response(output,request,clientSocket);
    			response.sendStaticResource();
    			output.close();
    			input.close();
       			clientSocket.close();    			
    		} catch (IOException e) {
    			HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
    			for(int i=0;i<e.getStackTrace().length;i++){
    				HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
    			}
    			e.printStackTrace();
    		}
    	}
    }
}
    
