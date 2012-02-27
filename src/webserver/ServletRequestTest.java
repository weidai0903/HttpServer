package edu.upenn.cis.cis555.webserver;

import java.io.BufferedReader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

/**
 * 
 * @author Wei Dai
 *
 */
public class ServletRequestTest extends TestCase {
	ServletRequest sr;

	protected void setUp() throws Exception {
		super.setUp();
		sr=new ServletRequest();
		sr.setHeaders("head1", "head contents");
		sr.setHeaders("head2", "hello");
		sr.setHeaders("head3", "nice one");
		sr.setHeaders("header3", "nice one, nice two, three, four");
		sr.setHeaders("If-Modified-Since", "Fri, 31 Dec 1999 23:59:59 GMT");
		sr.setHeaders("head4", "123");
		sr.setUri("/demo/ab/cd/ef/info?param=value&param2=value2");
		sr.setParameter("d", "1,2,3");
		sr.setParameter("e", "2");
		sr.setParameter("f", "1");
		sr.setBodyString("a=1&b=2&c=12345 \ne=6");
		sr.setAttribute("att1", "val1");
		sr.setAttribute("att2", "val2");
		sr.setAttribute("att3", "val3");
		sr.setHeaders("Cookie", "name=value; name2=value2; name3=value3");
		
	}
	
	public void testGetHeader(){
		assertEquals(sr.getHeader("head1"),"head contents");
		assertEquals(sr.getHeader("none"),null);
		assertEquals(sr.getHeader("head2"),"hello");
		assertEquals(sr.getHeader("head3"),"nice one");
	}
	public void testGetHeaders(){
		Enumeration<String> e;
		e=sr.getHeaders("header3");
		Vector<String> v=new Vector<String>();
		while(e.hasMoreElements()){
			v.add(e.nextElement());
			//System.out.println(values[i]+"XXX");
		}
		assertTrue(v.contains("nice one"));
		assertTrue(v.contains("nice two"));
		assertTrue(v.contains("three"));
		assertTrue(v.contains("four"));
	}
	public void testGetDateHeader(){
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Date testDate=null;
		try{
			testDate=dateFormat.parse("Fri, 31 Dec 1999 23:59:59 GMT");
		}catch(ParseException e){
			e.printStackTrace();
			return;
		}
		assertEquals(sr.getDateHeader("If-Modified-Since"),testDate.getTime());
	}
	public void testGetHeaderNames(){
		Enumeration<String> e;
		e=sr.getHeaderNames();
		Vector<String> v=new Vector<String>();
		while(e.hasMoreElements()){
			v.add(e.nextElement());
		}
		assertTrue(v.contains("head1"));
		assertTrue(v.contains("head2"));
		assertTrue(v.contains("head3"));
		assertTrue(v.contains("header3"));
		assertTrue(v.contains("If-Modified-Since"));
		ServletRequest sr2=new ServletRequest();
		e=sr2.getHeaderNames();
		assertFalse(e.hasMoreElements());
	}
	public void testGetIntHeader(){
		assertEquals(sr.getIntHeader("head4"),123);
	}
	public void testGetPathInfo(){
		assertEquals(sr.getPathInfo(),"/ab/cd/ef/info");
	}
	public void testGetQueryString(){
		assertEquals(sr.getQueryString(),"param=value&param2=value2");
	}
	public void testGetRequestURI(){
		assertEquals(sr.getRequestURI(),"/demo/ab/cd/ef/info");
	}
	public void testGetServletPath(){
		assertEquals(sr.getServletPath(),"/demo");
	}
	public void testGetParameterValues(){
		String[] params= new String[]{"1","2","3"};
		String[] params1= new String[]{"2"};
		String[] d=sr.getParameterValues("d");
		//System.out.println(a[0]);
		assertEquals(d.length,params.length);
		for(int i=0;i<d.length;i++){
			assertEquals(d[i],params[i]);
		}
		String[] e=sr.getParameterValues("e");
		assertEquals(e.length,params1.length);
		for(int i=0;i<e.length;i++){
			assertEquals(e[i],params1[i]);
		}
	}
	public void testGetParameter(){
		assertEquals(sr.getParameter("param"),"value");
		assertEquals(sr.getParameter("param2"),"value2");
		assertEquals(sr.getParameter("e"),"2");
		assertEquals(sr.getParameter("f"),"1");
	}
	public void testGetContentType(){
		sr.setHeaders("Content-Type", "html");
		assertEquals(sr.getContentType(),"html");
	}
//	public void testGetServerName(){
//		assertEquals(sr.getServerName(),"localhost");
//	}
	public void testGetReader(){
		BufferedReader br=null;
		try{
			br=sr.getReader();
		}catch(IOException e){
			e.printStackTrace();
		}
		String line=null,line2=null;
		try{
			line=br.readLine();
			line2=br.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}
		assertEquals(line,"a=1&b=2&c=12345 ");
		assertEquals(line2,"e=6");
	}
	public void testRemoveAttribute(){
		sr.removeAttribute("att1");
		assertEquals(sr.getAttribute("att1"),null);
		
	}
	public void testGetAttribute(){
		assertEquals(sr.getAttribute("att1"),"val1");
		assertEquals(sr.getAttribute("att2"),"val2");
		assertEquals(sr.getAttribute("att3"),"val3");
	}
	public void testGetCookies(){
		Cookie c[]=sr.getCookies();
		Cookie c1[]=new Cookie[3];
		c1[0]=new Cookie("name","value");
		c1[1]=new Cookie("name2","value2");
		c1[2]=new Cookie("name3","value3");
		for(int i=0; i<c1.length;i++){
			assertEquals(c[i].getName(),c1[i].getName());
			assertEquals(c[i].getValue(),c1[i].getValue());
		}
	}
	
	
	
	

}
