package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public class OutboundQueue implements Queue{

	private LinkedBlockingDeque<TaskChannel> outbound;
	
	public OutboundQueue() {
		outbound = new LinkedBlockingDeque<TaskChannel>();
	}

	@Override
	public void put(Task task, Channel channel) {
	
		try {
			outbound.put(new TaskChannel(task, channel));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public TaskChannel get() {
		try {
			return outbound.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return outbound.size();
	}

	
}
