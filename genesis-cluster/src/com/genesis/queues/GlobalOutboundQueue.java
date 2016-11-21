package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import global.Global.GlobalMessage;

public class GlobalOutboundQueue {

	LinkedBlockingDeque<GlobalMessage> globalOutboundQueue = 
			new LinkedBlockingDeque<GlobalMessage>();
	
	public GlobalOutboundQueue() {
		
	}
	
	public GlobalMessage take(){
		if(globalOutboundQueue.size() > 0)
			return globalOutboundQueue.pollFirst();
		return null;
	}
	
	public void put(GlobalMessage msg){
		try {
			globalOutboundQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int size(){
		return globalOutboundQueue.size();
	}
}
