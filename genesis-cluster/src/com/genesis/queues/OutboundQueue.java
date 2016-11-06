package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;

public class OutboundQueue implements Queue{

	private static Logger logger = LoggerFactory.getLogger("outboud queue");
	private LinkedBlockingDeque<WorkChannel> outbound;
	
	public OutboundQueue() {
		outbound = new LinkedBlockingDeque<WorkChannel>();
	}

	@Override
	public void put(WorkMessage work, Channel channel) {
		outbound.add(new WorkChannel(work, channel));
	}

	@Override
	public WorkChannel get() {
		try {
			return outbound.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSize() {
		return outbound.size();
	}
	
	
	public WorkMessage getWorkMessage(){
		WorkChannel tc= get();
		if(tc != null ){
			return tc.getWorkMessage();
		}
		return null;
	}
	
	public boolean process(){
		if(outbound.size() == 0) {
			logger.info("outbound queue size is 0, process other queues, may be lazy ?");
			return false;
		}
		WorkChannel t = get();
		WorkMessage work = t.getWorkMessage();
		Channel channel = t.getChannel();
		if(channel.isActive() && channel.isOpen())
			channel.writeAndFlush(work);
		return true;
	}

}
