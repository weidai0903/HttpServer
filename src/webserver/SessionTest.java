package edu.upenn.cis.cis555.webserver;

import java.util.Date;

import junit.framework.TestCase;
/**
 * 
 * @author Wei Dai
 *
 */
public class SessionTest extends TestCase {
	Session session;

	protected void setUp() throws Exception {
		session=new Session();
	}
	public void testGetId(){
		session.setId("322222");
		assertEquals(session.getId(),"322222");
	}
	public void testGetLastAccessedTime(){
		session.setLastAccessedTime(1234);
		assertEquals(session.getLastAccessedTime(),1234);
	}
	public void testGetMaxInactiveInterval(){
		session.setMaxInactiveInterval(30);
		assertEquals(session.getMaxInactiveInterval(),30);
	}
	public void testGetAttribute(){
		session.setAttribute("a1","v1");
		assertEquals(session.getAttribute("a1"),"v1");
		session.removeAttribute("a1");
		assertEquals(session.getAttribute("a1"),null);
	}
	public void testInvalidate(){
		assertTrue(session.isValid());
		session.invalidate();
		assertFalse(session.isValid());
	}
	public void testIsNew(){
		assertTrue(session.isNew());
		session.setNotNew();
		assertFalse(session.isNew());
	}
	
	
	

}
