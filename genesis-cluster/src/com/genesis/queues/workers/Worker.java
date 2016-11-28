package com.genesis.queues.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.queues.InboundQueue;
import com.genesis.queues.Queue;
import com.genesis.router.server.ServerState;

public class Worker extends Thread{

	private static Logger logger = LoggerFactory.getLogger("worker");
	
	private ServerState state;
	
	private long pause;
	
	private int id;
		
	public Worker(ServerState state,long pause,int id){
		this.state = state;
		this.pause = pause;
		this.id = id;
	}
	
	

	@Override
	public void run() {
		while(1==1) {
			try{
				if(state != null && state.getQueueMonitor() != null) {
					Queue queue = state.getQueueMonitor().getQueue();
					queue.process();
				}
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	
}
