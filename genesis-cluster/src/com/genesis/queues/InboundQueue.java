package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import com.genesis.router.server.ServerState;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public class InboundQueue implements Queue{

	private LinkedBlockingDeque<TaskChannel> inbound = null;
	
	
	public InboundQueue(){
		inbound = new LinkedBlockingDeque<TaskChannel>();
	}
	@Override
	public void put(Task task, Channel channel) {
		inbound.add(new TaskChannel(task,channel));
	}

	@Override
	public TaskChannel get() {
		try {
			return inbound.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public int getSize() {
		return inbound.size();
	}
	
	
}
