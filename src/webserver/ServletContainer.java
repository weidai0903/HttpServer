package edu.upenn.cis.cis555.webserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Wei Dai
 * @author Todd J. Green, modified by Nick Taylor
 */
public class ServletContainer {	
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if(qName.compareTo("url-pattern")==0){
				m_state=100;
			} else if(qName.compareTo("session-timeout")==0) {
				m_state=200;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if(m_state==100){
				m_urls.put(value, m_servletName);
				m_state=0;
			} else if(m_state==200){
				sessionTimeout=value;
				m_state=0;
			}
			else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}
		}
		private int m_state = 0;
		//private String m_url;
		private String m_servletName;
		private String m_paramName;
		HashMap<String,String> m_urls=new HashMap<String,String>();
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
		String sessionTimeout;
	}
	/**
	 * parse the web.xml file
	 * @param webdotxml
	 * @return handler
	 * @throws Exception
	 */
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private static Context createContext(Handler h) {
		Context c = new Context();
		for (String param : h.m_contextParams.keySet()) {
			c.setInitParam(param, h.m_contextParams.get(param));
		}
		return c;
	}
	/**
	 * load all the servlets 
	 * @param h
	 * @param context
	 * @return hashMap of servlets 
	 * @throws Exception
	 */
	private static HashMap<String,HttpServlet> createServlets(Handler h, Context context){
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			Config config = new Config(servletName, context);
			String className = h.m_servlets.get(servletName);
			//h.m_urls.get(key);
			//System.out.println(className);
			HttpServlet servlet=null;
			try{
			Class servletClass = Class.forName(className);
			servlet = (HttpServlet) servletClass.newInstance();
			}catch(Exception e){
				e.printStackTrace();
			}
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			try{
			servlet.init(config);
			}catch(Exception e){
				e.printStackTrace();
			}
			servlets.put(servletName, servlet);
		}
		return servlets;
	}
	private static HashMap<String,String> createUrls(Handler h){
		return h.m_urls;
	}

	private static String createSessionTimeout(Handler h){
		return h.sessionTimeout;
	}
	

	/**
	 * if a servlet with the specific servletName exists, return true
	 */
	public boolean servletExist(String servletName){
		if(servlets.containsKey(servletName)) return true;
		else return false;
	}
	static Handler h;
	static Context context;
	static HashMap<String,HttpServlet> servlets;
	static HashMap<String,String> urls;
	String sessionTimeout;
	//Session session;
	OutputStream output;
	ArrayList<String> headers;
	//Socket socket;
	static String XMLFilePath;
	
	/**
	 * destroy all the servlets before terminate the server
	 * @return true if destroyed successfully
	 */
	protected static boolean destroyServlets(){
			Set<String> set=servlets.keySet();
			Iterator<String> iter=set.iterator();
			while(iter.hasNext()){
				String name=iter.next();
				servlets.get(name).destroy();
			}
			return true;
	}
	public static void setup(String XMLFilePathStr) throws Exception{
		h = parseWebdotxml(XMLFilePathStr);
		XMLFilePath=XMLFilePathStr;
		context = createContext(h);
		context.setPath(XMLFilePath);
		servlets = createServlets(h, context);

	}
	ServletContainer(String XMLFilePath, OutputStream output, ArrayList<String> headers) throws Exception{
		//h = parseWebdotxml(XMLFilePath);
		//this.XMLFilePath=XMLFilePath;
		//context = createContext(h);
		//context.setPath(XMLFilePath);
		this.output=output;
		//this.socket=socket;
		this.sessionTimeout=createSessionTimeout(h);
		urls = createUrls(h);
		//servlets = createServlets(h, context);
		//session = null;
		this.headers=headers;
	}
	public static HashMap<String,String> getUrls(){
		return urls;
	}
	//Store sessions
	private static HashMap<String,Session> sessions=new HashMap<String,Session>();
	public static HashMap<String,Session> getSessions(){
		return sessions;
	}
	/**
	 * Run the servlet with the corresponding servletName
	 * @param servletName
	 * @param HTTPMethod
	 * @param uri
	 * @param protocol
	 * @param postBody
	 * @return true if sussessful
	 * @throws Exception
	 */
	public boolean runServlets(String servletName, String HTTPMethod,String uri,String protocol,String postBody) throws Exception {
		
		//for (int i = 1; i < args.length - 1; i += 2) {
			ServletResponse response = new ServletResponse();
			ServletRequest request = new ServletRequest(sessions,response,context,sessionTimeout);
			Iterator<String> i=headers.iterator();
			while(i.hasNext()){
				String line=i.next();
				String subStrings[]=line.split(":", 2);
				if(subStrings.length<2){
					HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
					HttpServer.errorLog.append("<p>"+"Invalid headers"+"</p>");
					System.out.println("Invalid headers");
					return false;
				}
				//System.out.println(line);
				request.setHeaders(subStrings[0].trim(), subStrings[1].trim());
			}
			request.setUri(uri);
			request.setProtocol(protocol);
			if(HTTPMethod.equals("POST")){
				request.setBodyString(postBody);
			}
			request.setIPAddress(Worker.getClientIP());
			response.setOutPutStream(output);
			String[] strings = servletName.split("\\?|&|=");
			if (!servletExist(strings[0])) {
				HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
				HttpServer.errorLog.append("<p>"+"Socket mapping error"+"</p>");
				System.err.println("error: cannot find mapping for servlet " + strings[0]);
				return false;
			}
			HttpServlet servlet = servlets.get(strings[0]);			
			for (int j = 1; j < strings.length - 1; j += 2) {
				request.setParameter(strings[j].trim(), strings[j+1].trim());
			}
			if (HTTPMethod.compareTo("GET") == 0 || HTTPMethod.compareTo("POST") == 0) {
				request.setMethod(HTTPMethod);
				servlet.service(request, response);
				response.flushBuffer();
				
			} else {
				HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
				HttpServer.errorLog.append("<p>"+"error: expecting 'GET' or 'POST'"+"</p>");
				System.err.println("error: expecting 'GET' or 'POST', not '" + HTTPMethod + "'");
				return false;
			}
			
			//session = (Session) request.getSession(false);
			return true;
			
		//}
	}
}
 
