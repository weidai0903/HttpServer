package edu.upenn.cis.cis555.webserver;

import java.net.Socket;

/**
 * CIS555 HW1MS1
 * @author Wei Dai (weidai)
 *
 */

public class ThreadPool{
	private static final int poolSize=2000;
	private int workerSize;
	private Worker workForce[];
	private static Thread[] workingThreads;
	public static Thread[] getWorkingThreads(){
		return workingThreads;
	}
	//constructor is given how many worker needed
	ThreadPool(int n){
		workerSize=n;
		workForce=new Worker[workerSize];
	}
	static Monitor mon=new Monitor();
	protected void handleSocket(Socket s){
		//insert sockets into the queue
		mon.insert(s);
	}
	/**
	 * create all the workers and let them wait for the sockets
	 */
	protected void assignWorker(){
		workingThreads=new Thread[workerSize];
		for(int i=0;i<workerSize;i++){
			workForce[i]=new Worker();
			workingThreads[i]=new Thread(workForce[i]);
			workingThreads[i].start();
		}
	}
	//use monitor to solve concurrency problem
	protected static class Monitor{
		Socket sockets[]=new Socket[poolSize];
		private int count=0, low=0, high=0;
		//assign the new socket into the queue
		public synchronized void insert(Socket s){
			if(count>=poolSize){goToSleep(); }
			sockets[high]=s;
			high=(high+1)%poolSize;
			count++;
			if(count==1) notify();
		}
		//remove one socket from the queue
		public synchronized Socket remove(){
			Socket s;
			if(count<=0) {goToSleep();}
			s=sockets[low];
			low=(low+1)%poolSize;
			count--;
			if(count==poolSize-1) notify();
			return s;
		}
		private void goToSleep(){
			try{
				wait();
			}catch(InterruptedException e){
				HttpServer.errorLog.append("<p>"+"Error stack trace:"+"</p>");
				for(int i=0;i<e.getStackTrace().length;i++){
					HttpServer.errorLog.append("<p>"+e.getStackTrace()[i].toString()+"</p>");
				}
				System.out.println(Thread.currentThread().getName()+" interrupted");
			}
		}
	}
}
