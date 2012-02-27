package edu.upenn.cis.cis555.webserver;

import java.io.*;
import java.util.ArrayList;
/**
 * HW1MS1
 * @author Wei Dai
 *
 */

public class Request {

	private InputStream input;
	private String uri=null;
	private String httpMethod=null;
	private String ifModifiedDate=null;
	private InputStreamReader reader;
	private ArrayList<String> headers;
	private String protocol;
	private String postBodyStr=null;
	public String getPostBody(){
		return postBodyStr;
	}
	public String getProtocol(){
		return protocol;
	}
	public Request(InputStream input) {
		this.input = input;
	}
	public ArrayList<String> getHeaders(){
		return this.headers;
	}
	/**
	 * @return parsed uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @return httpMethod
	 */
	public String httpMethod(){
		return httpMethod;
	}
	/**
	 * @return if-modified-date
	 */
	public String getIfModifiedDate(){
		return ifModifiedDate;
	}
	public void parse() {
		reader=new InputStreamReader(input);
		BufferedReader br=new BufferedReader(reader);
		String requestStr="";
		try{	
			requestStr=br.readLine();
		}catch(IOException e){
			HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
			for(int i=0;i<e.getStackTrace().length;i++){
				HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
			}
			System.out.println("Error read in headers");
		}
		if(requestStr!=null){
			//parse the first status line
		int i, j;
		//parse the uri and http method
		i = requestStr.indexOf(' ');
		if (i>0) {
			httpMethod=requestStr.substring(0,i);
			j = requestStr.indexOf(' ', i + 1);
			if(j==-1) j=requestStr.length();
			if (j > i)
			{
				uri= requestStr.substring(i + 1, j);
			}
			protocol=requestStr.substring(j).trim();
			boolean hostName=false;
			headers=new ArrayList<String>(); 
			int contentLen=-1;
			while(!requestStr.equals("")){
				if(requestStr.contains(":")) headers.add(requestStr);
				//System.out.println(requestStr);
				int index1st=0;
				int index2nd=0;
				String hostNameStr;
				//compare the hostname in request and the one in socket
				if(requestStr.startsWith("Host:")){
					while(index1st<requestStr.length()&&requestStr.charAt(index1st)!=':'){
						index1st++;
					}
					index2nd=index1st+1;
					while(index2nd<requestStr.length()&&requestStr.charAt(index2nd)!=':'){
						index2nd++;
					}
					hostNameStr=requestStr.substring(index1st+1,index2nd).trim();
					if(hostNameStr.equals(Worker.getServerHostName())) hostName=true;
				}
				//get if-modified-date
				if(requestStr.startsWith("If-Modified-Since")){
					int index1=0;
					while(requestStr.charAt(index1)!=':'&&index1<requestStr.length()) index1++;
					ifModifiedDate=requestStr.substring(index1+1).trim();
				}
				
				if(requestStr.startsWith("Content-Length")){
					int index1=0;
					while(requestStr.charAt(index1)!=':'&&index1<requestStr.length()) index1++;
					try{
						contentLen=Integer.parseInt(requestStr.substring(index1+1).trim());
					}catch(NumberFormatException e){
						System.out.println("Content-Length Wrong!");
						contentLen=0;
					}
				}
				try{
					//read in another line from headers
					requestStr=br.readLine();
				}catch(IOException e){
					HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
					for(int s=0;s<e.getStackTrace().length;s++){
						HttpServer.errorLog.append("<p>"+e.getStackTrace()[s].toString()+"</p>");
					}
					System.out.println("Error read in headers");
				}
			}
			if(protocol!=null&&protocol.equals("HTTP/1.0"))  hostName=true;
			
			if(httpMethod.equals("POST")) {
				if(contentLen==-1){
					HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
					HttpServer.errorLog.append("<p>"+"POST without Content-Length header"+"</p>");
					//contentLen=1024;
					StringBuilder postBody=new StringBuilder();
					try{
						requestStr=br.readLine();
					}catch(IOException e){
						HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
						HttpServer.errorLog.append("<p>"+"No post body"+"</p>");
					}
					while(requestStr!=null&&!requestStr.equals("")){
						//if(requestStr==null||requestStr.equals("")) break;
						postBody.append(requestStr);
						postBody.append("\n");
						try{
							requestStr=br.readLine();
						}catch(IOException e){
							HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
							for(int s=0;s<e.getStackTrace().length;s++){
								HttpServer.errorLog.append("<p>"+e.getStackTrace()[s].toString()+"</p>");
							}
							e.printStackTrace();
						}
					}
					postBodyStr=postBody.toString();
				}else{
					char[] str=new char[0];
					try{
						//requestStr=br.readLine();
						str=new char[contentLen];
						int numRead=br.read(str, 0, contentLen);
					}catch(IOException e){
						HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
						for(int s=0;s<e.getStackTrace().length;s++){
							HttpServer.errorLog.append("<p>"+e.getStackTrace()[s].toString()+"</p>");
						}
						e.printStackTrace();
					}
					postBodyStr=String.copyValueOf(str);
				}

			}
			if(hostName==false){
				uri=null;
				httpMethod=null;
			}
		}
		else{
			httpMethod=null;
			uri=null;
		}
		}		
	}
}
