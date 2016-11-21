package com.genesis.queues.workers;

import com.genesis.queues.GlobalOutboundQueue;
import com.genesis.router.server.ServerState;

public class GlobalOutWorker extends Thread{

	private GlobalOutboundQueue globalQueue;
	private ServerState state = null;
	public GlobalOutWorker(ServerState state){
		this.state = state;
		globalQueue = state.getGlobalOutboundQueue();
	}
	
	public void start(){
		while(globalQueue.size() > 0){
			state.getgMon().pushMessagesIntoCluster(globalQueue.take());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
