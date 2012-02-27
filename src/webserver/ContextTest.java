package edu.upenn.cis.cis555.webserver;

import junit.framework.TestCase;

public class ContextTest extends TestCase {
	Context context;

	protected void setUp() throws Exception {
		context=new Context();
		context.setAttribute("a1", "value1");
		context.setAttribute("a2", "value2");
		context.setInitParam("p1", "param1");
		context.setInitParam("p2", "param2");
	}
	public void testGetAttribute(){
		assertEquals(context.getAttribute("a1"),"value1");
		assertEquals(context.getAttribute("a2"),"value2");
	}
	public void testGetContext(){
		assertEquals(context.getContext(""),context);
	}
	public void testGetInitParameter(){
		assertEquals(context.getInitParameter("p1"),"param1");
		assertEquals(context.getInitParameter("p2"),"param2");
	}
	public void testGetMajorVersion(){
		assertEquals(context.getMajorVersion(),2);
	}
	public void testGetMinorVersion(){
		assertEquals(context.getMinorVersion(),4);
	}
	public void testGetRealPath(){
		context.setPath("/a/b/c/web.xml");
		assertEquals(context.getRealPath("/foo/bar.html"),"/a/b/c/classes/foo/bar.html");
	}
	public void testRemoveAttribute(){
		context.removeAttribute("a1");
		assertEquals(context.getAttribute("a1"),null);
		context.removeAttribute("a2");
		assertEquals(context.getAttribute("a2"),null);
	}
	
	

}
