package com.genesis.queues;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public interface Queue {

	void put(Task task,Channel channel);
	
	TaskChannel get();
		
	int getSize();
}
