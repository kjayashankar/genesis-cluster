package com.genesis.monitors;

import com.genesis.queues.InboundQueue;
import com.genesis.queues.LazyQueue;
import com.genesis.queues.OutboundQueue;
import com.genesis.queues.Queue;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.tasks.Rebalancer;

public class QueueMonitor {

	private Queue inboundQueue;
	
	private Queue outboundQueue;
	private int inQ = 0;
	private int outq = 0;
	
	
	private Queue lazyQueue;
	
	private int flag = 1;
	
	private int workerthreads = 0;
	
	private ServerState state;
	
	public QueueMonitor(int workerThreads , ServerState state, Rebalancer balancer){
		this.state = state;
		inboundQueue = new InboundQueue(balancer);
		outboundQueue = new OutboundQueue();
		lazyQueue = new LazyQueue();
		this.workerthreads = workerThreads;
	}
	
	public Queue getQueue(){
		if(state != null && !idleStatus() ){
			if(flag == 0){
				flag++;
				return inboundQueue;
			}
			else {
				flag--;
				return outboundQueue;
			}
		}
		else if( state != null) {
			return lazyQueue;
		}
		//default inbound queue;
		return inboundQueue;
	}
	
	private boolean idleStatus() {
		return false;
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
	
	public Queue getLazyQueue() {
		return lazyQueue;
	}
}
