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
			logger.info("worker index "+id+", ");
			// TODO Auto-generated method stub
			if(state != null && state.getQueueMonitor() != null) {
				Queue queue = state.getQueueMonitor().getQueue();
				if(queue instanceof InboundQueue){
					logger.info("worker index "+id+", performing inbound tasks ");
		
					// treat inbound tasks
				}
				else{
					logger.info("worker index "+id+", performing outbound tasks ");
					/*TaskChannel tc = queue.get();
					Channel channel = tc.getChannel();
					if(channel != null && channel.isActive())
						channel.writeAndFlush(tc);*/
				}
				queue.process();
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
}
