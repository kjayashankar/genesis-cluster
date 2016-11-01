package com.genesis.helper;

import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;

public interface ServerHandler {

	void handleTask(WorkMessage msg, Channel channel);
	
	void handleBeat(WorkMessage msg, Channel channel);
	
	void handleMessage(WorkMessage msg,Channel channel);

	void handleState(WorkMessage msg, Channel channel);
	
	void handleVote(WorkMessage msg, Channel channel);
		
}
