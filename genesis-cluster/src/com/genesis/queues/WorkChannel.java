package com.genesis.queues;

import io.netty.channel.Channel;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;

public class WorkChannel {

	private WorkMessage work;
	
	private Channel channel;

	public WorkChannel(WorkMessage work, Channel channel) {
		// TODO Auto-generated constructor stub
		this.work = work;
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

	public WorkMessage getWorkMessage() {
		return work;
	}

	public void setTask(WorkMessage work) {
		this.work = work;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	
}
