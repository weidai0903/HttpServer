package edu.upenn.cis.cis555.webserver;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;
/**
 * 
 * @author Wei Dai
 *
 */
public class ServletResponseTest extends TestCase {
	ServletResponse sr;

	protected void setUp() throws Exception {
		sr=new ServletResponse();
		Cookie c1=new Cookie("name","value");
		sr.addCookie(c1);
		Date now=new Date();
		//sr.setDateHeader("Date", now.getTime());
		sr.setHeader("Content-Type", "html");
		sr.setIntHeader("Content-Length", 1024);
		sr.setStatus(200);
		sr.setContentLength(2048);
	}

	public void testAddCookie() {
		Cookie c1=new Cookie("name","value");
		sr.addCookie(c1);
	}

	public void testContainsHeader() {
		assertTrue(sr.containsHeader("Content-Type"));
		assertFalse(sr.containsHeader("hello"));
	}

	public void testGetContentType() {
		assertEquals(sr.getContentType(),"html");
	}

	public void testSetContentType() {
		sr.setContentType("html");
	}

	public void testSetBufferSize() {
		sr.setBufferSize(1024);
		assertEquals(sr.getBufferSize(),1024);
	}

	public void testFlushBuffer() {
		try{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			sr.setOutPutStream(baos);
			PrintWriter pw;
			pw=sr.getWriter();
			pw.println("XXX");
			sr.flushBuffer();
			//System.out.print(baos.toString());
			assertEquals(baos.toString().trim(),"HTTP/1.1 200 OK\r\n"+
			"Content-Length: 2048\r\n"+
			"Content-Type: html\r\n"+
			"Set-Cookie: name=value\r\n"+
			"\r\n"+
			"XXX");
		}catch(IOException e){
			e.printStackTrace();
		}
		assertTrue(sr.isCommitted());
	}

	public void testResetBuffer() {
		sr.resetBuffer();
		BufferedOutputStream bos=sr.getBOS();
	}

	public void testReset() {
		sr.reset();
		assertTrue(sr.getHeaders().isEmpty());
		try{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			sr.setOutPutStream(baos);
			PrintWriter pw=sr.getWriter();
			pw.println("XXX");
			sr.reset();
			assertEquals(baos.toString().trim(),"");
			sr.flushBuffer();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void testSetLocale() {
		sr.setLocale(Locale.PRC);
		assertEquals(sr.getLocale(),Locale.PRC);
	}

}
