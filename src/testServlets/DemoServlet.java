import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class DemoServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		//response.addHeader("Content-Length", "1024");
		//response.sendRedirect("/");
		int error=request.getIntHeader("Host");
		String x=request.getServerName();
		int y=request.getServerPort();
		String z=request.getRemoteAddr();
		String w=request.getRemoteHost();
		StringBuffer sb=request.getRequestURL();
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Hello!</P>");
		out.println(error);
		out.println("<P>Hello!</P>");
		out.println(x);
		out.println(y);
		out.println(z);
		out.println(w);
		//System.out.println("XXX");
		out.println(sb.toString());
		out.println("</BODY></HTML>");
		//response.sendError(404, "file not found");
		response.flushBuffer();
		out.println(y);
		out.println(z);
		out.println(w);
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		BufferedReader br=request.getReader();
		int l=request.getContentLength();
		String param=request.getParameter("post1");
		String param2=request.getParameter("post2");
		response.setContentLength(100);
		response.setContentType("html/txt");
		String line=br.readLine();
		PrintWriter out = response.getWriter();
		while(line!=null){
			out.println("<HTML>"+line+"</HTML>");
			line=br.readLine();
		}
		out.println("<HTML>"+l+"</HTML>");
		out.println("<HTML>"+param+"</HTML>");
		out.println("<HTML>"+param2+"</HTML>");
	
	}
	
}
