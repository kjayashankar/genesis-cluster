package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import global.Global.GlobalMessage;
import io.netty.channel.Channel;

public class GlobalQueue {

	LinkedBlockingDeque<GlobalChannel> globalOutboundQueue = 
			new LinkedBlockingDeque<GlobalChannel>();
	
	public GlobalQueue() {
		
	}
	
	public void put(GlobalMessage msg,Channel channel){
		try {
			globalOutboundQueue.put(new GlobalChannel(msg,channel));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void process(){
		if(globalOutboundQueue != null && globalOutboundQueue.size() > 0){
			GlobalChannel gm = globalOutboundQueue.pollFirst();
			if(gm.channel != null && gm.channel.isActive() && gm.channel.isOpen() && gm.channel.isWritable() )
				gm.channel.writeAndFlush(gm.msg);
			else
				globalOutboundQueue.add(gm);
		}
	}
	
	public int size(){
		return globalOutboundQueue.size();
	}
	
	
	
	
	
	class GlobalChannel{
		
		public GlobalMessage msg;
		
		public Channel channel;
		
		public GlobalChannel(GlobalMessage msg,Channel channel){
			this.msg = msg;
			this.channel = channel;
		}
	}
}
