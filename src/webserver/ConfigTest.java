package edu.upenn.cis.cis555.webserver;

import junit.framework.TestCase;

public class ConfigTest extends TestCase {
	Config config;
	Context context;
	protected void setUp() throws Exception {
		config=new Config("servletone",context);
		config.setInitParam("a", "1");
		config.setInitParam("b", "2");
		
	}
	public void testGetInitParameter(){
		assertEquals(config.getInitParameter("a"),"1");
		assertEquals(config.getInitParameter("b"),"2");
	}
	public void testGetServletContext(){
		assertEquals(config.getServletContext(),context);
	}
	public void testGetServletName(){
		assertEquals(config.getServletName(),"servletone");
	}


}
