package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.ServerState;
import com.genesis.router.server.tasks.Rebalancer;

import io.netty.channel.Channel;
import pipe.work.Work.WorkMessage;

public class InboundQueue implements Queue{

	LinkedBlockingDeque<WorkChannel> inbound;
	
	private int balanced;
	private ServerState state;
	Rebalancer rebalance ;
	private static Logger logger = LoggerFactory.getLogger("inbound queue");
	
	public InboundQueue(ServerState state,Rebalancer newBalancer) {
		this.state = state;
		this.rebalance = newBalancer;
		inbound = new LinkedBlockingDeque<WorkChannel>();
		
	}
	
	@Override
	public void put(WorkMessage work, Channel channel) {
		inbound.add(new WorkChannel(work, channel));
	}

	@Override
	public WorkChannel get() {
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
	
	
	public WorkMessage getWorkMessage(){
		WorkChannel tc= get();
		if(tc != null ){
			return tc.getWorkMessage();
		}
		return null;
	}
	
	public boolean process(){
		if(inbound.size() == 0) {
			logger.info("inbound queue size is 0, process other queues, may be lazy ?");
			return false;
		}
		
		WorkChannel t = get();
		WorkMessage work = t.getWorkMessage();
		/*Task task
		Channel channel = t.getChannel();
		if(channel.isActive() && channel.isOpen())
			channel.writeAndFlush(work);
		state
		return true;*/
		return true;
	}
	
	public WorkChannel rebalance() {
		WorkChannel t = null;

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
