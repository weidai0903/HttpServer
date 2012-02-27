package edu.upenn.cis.cis555.webserver;

import java.net.*;
import java.io.IOException;
/**
 * CIS555 HW1MS1 HTTP server
 * @author Wei Dai (weidai)
 * @version 1.0
 *
 */
public class HttpServer implements Runnable{
	
  private static ServerSocket serverSocket = null;
  protected static boolean controlMode=false;
  //private Thread runningThread= null;
  public static String rootDir;
  public int port;
  //create a thread pool with 15 workers
  protected ThreadPool threadPool = new ThreadPool(12);
  public static Thread serverT;
  public static int serverPortNum;
  //check if shutdown
  public static boolean stopped=false; 
  public static String XMLFilePath;
  public static StringBuilder errorLog=new StringBuilder();
  public static ServerSocket getServerSocket(){
	  return serverSocket;
  }
  public static void main(String[] args) {
	  if(args.length==3){
		  XMLFilePath=args[2];
	  HttpServer server = new HttpServer();
	  server.port=Integer.parseInt(args[0]);
	  rootDir=args[1];
	  serverT=new Thread(server);
	  //dispatcher start to work
	  serverT.start();
	  System.out.println("Http Server start running");
	  //thread start, preparing thread pool and add jobs to it.
	  try {
		  	//wait for dispatcher die
		  	serverT.join();
		  	System.out.println("Dispatcher thread end");
	  		//Thread.currentThread().join();
	  } catch (InterruptedException e) {
    	  HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
    	  HttpServer.errorLog.append("<p>"+"Socket dispatcher interrupted"+"</p>");
	      e.printStackTrace();
	  }
	  Thread[] workerThreads=ThreadPool.getWorkingThreads();
	  //wait for all worker threads die
	  for(int i=0;i<workerThreads.length;i++){
		  try{
			  workerThreads[i].join();
		  }catch(InterruptedException e){
			  System.out.println("waiting for all workers die not succussful");
		  }
	  }
	  System.out.println("All threads stopped");
	  //destroy all running servlets
	  if(!ServletContainer.destroyServlets()){
    	  HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
    	  HttpServer.errorLog.append("<p>"+"Servlets destroy unsuccessful:"+"</p>");
	  }
	  System.out.println("All servlets destroyed");
	  }
	  else System.out.println("Wei Dai (weidai)");
  }
  public static String serverTState(){
	  if(serverT.getState()==Thread.State.WAITING) return "waiting";
	  else return "running";
  }
  public void run(){
      try {
    	  try{
    		  ServletContainer.setup(XMLFilePath);
    	  }catch(Exception e){
    		  e.printStackTrace();
        	  HttpServer.errorLog.append("<p>"+"Error:"+"</p>");
        	  HttpServer.errorLog.append("<p>"+"Servlets load unsuccessful"+"</p>");
    	  }
          serverSocket = new ServerSocket(this.port);
          //System.out.println(serverSocket);
      } catch (IOException e) {
    	  HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
    	  for(int i=0;i<e.getStackTrace().length;i++){
				HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
    	  }
    	  throw new RuntimeException("Cannot open port 8080", e);
      }
      //tell the threadPool to let the workForce wait
      this.threadPool.assignWorker();
      while(true){
          Socket clientSocket = null;
          //System.out.println("creating client socket");
          try {
              clientSocket = serverSocket.accept();
              serverPortNum=serverSocket.getLocalPort();
              if(clientSocket.isClosed()==true) {System.out.println("Client Socket closed");break;}
              this.threadPool.handleSocket(clientSocket);
          } catch (IOException e) {
        	  HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
        	  for(int i=0;i<e.getStackTrace().length;i++){
        		  HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
        	  }
  			  System.out.println("Server Socket closed");
        	  break;
          }
      }
  }
}