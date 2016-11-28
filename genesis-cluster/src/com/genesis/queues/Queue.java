package com.genesis.queues;

import io.netty.channel.Channel;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;

public interface Queue {

	void put(WorkMessage workMessage,Channel channel);
	
	WorkChannel get();
		
	int getSize();
	
	void process();
	
	WorkMessage rebalance();

	int numEnqueued();

	int numProcessed();
	
	


}
