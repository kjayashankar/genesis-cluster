package com.genesis.queues;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public class TaskChannel {

	private Task task;
	
	private Channel channel;

	public TaskChannel(Task task, Channel channel) {
		// TODO Auto-generated constructor stub
		this.task = task;
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	
}
