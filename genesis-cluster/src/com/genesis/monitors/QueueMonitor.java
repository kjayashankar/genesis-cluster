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
		inboundQueue = new InboundQueue(state,balancer);
		outboundQueue = new OutboundQueue(state);
		lazyQueue = new LazyQueue(state);
		this.workerthreads = workerThreads;
	}
	
	public Queue getQueue(){
		if(state != null && idleStatus() ){
			return lazyQueue;
		}
		else if(load(inboundQueue) > load(outboundQueue))
			return inboundQueue;
		
		return outboundQueue;
	}

	private int load(Queue queue){
		return queue.getSize();
	}
	
	private boolean idleStatus() {
		return outboundQueue.getSize() == 0 && inboundQueue.getSize() == 0 ;
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
