package com.genesis.queues.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.queues.GlobalQueue;
import com.genesis.router.server.ServerState;

public class GlobalWorker implements Runnable{

	private static Logger logger = LoggerFactory.getLogger("global worker");
	private GlobalQueue globalQueue;
	private ServerState state = null;
	private boolean forever = true;
	public GlobalWorker(ServerState state){
		this.state = state;
		globalQueue = state.getGlobalOutboundQueue();
	}
	
	@Override
	public void run() {
		
		while(forever) {
			logger.info("global outbound worker");
			
			try {
				if(state.getgMon() != null ) {
					state.getgMon().getQueue().process();
				}
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
}
