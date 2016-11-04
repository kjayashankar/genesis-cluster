package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.tasks.Rebalancer;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public class InboundQueue implements Queue{

	private static Logger logger = LoggerFactory.getLogger("inbound queue");
	
	private LinkedBlockingDeque<TaskChannel> inbound = null;

	private int balanced;

	Rebalancer rebalance;
	
	public InboundQueue(Rebalancer balance){
		inbound = new LinkedBlockingDeque<TaskChannel>();
		this.rebalance = balance;
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
	
	public TaskChannel rebalance() {
		TaskChannel t = null;

		try {
			if (rebalance != null && !rebalance.allow())
				return t;

			t = inbound.take();
			balanced++;
		} catch (InterruptedException e) {
			logger.error("failed to rebalance a task", e);
		}
		return t;
	}
	
}
