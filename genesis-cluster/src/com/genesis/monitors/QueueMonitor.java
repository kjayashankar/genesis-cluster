package com.genesis.monitors;

import com.genesis.queues.InboundQueue;
import com.genesis.queues.OutboundQueue;
import com.genesis.queues.Queue;
import com.genesis.router.server.ServerState;

public class QueueMonitor {

	private Queue inboundQueue;
	
	private Queue outboundQueue;
	
	private int flag = 1;
	
	private ServerState state;
	
	public QueueMonitor(ServerState state){
		this.state = state;
		inboundQueue = new InboundQueue();
		outboundQueue = new OutboundQueue();
	}
	
	public Queue getQueue(){
		if(state != null){
			if(flag == 0){
				flag++;
				return inboundQueue;
			}
			else {
				flag--;
				return outboundQueue;
			}
		}
		//default inbound queue;
		return inboundQueue;
	}
	
	public void clearFlags(){
		flag = 1;
	}
	
	public Queue getInboundQueue(){
		return inboundQueue;
	}
	
	public Queue getOutboundQueue() {
		return outboundQueue;
	}
}
