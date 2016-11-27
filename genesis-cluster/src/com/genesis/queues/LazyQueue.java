package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.helper.TaskHandler;
import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeInfo;
import com.google.protobuf.ByteString;

import io.netty.channel.Channel;
import pipe.common.Common.Node;
import pipe.work.Work.TaskType;
import pipe.work.Work.WorkMessage;

public class LazyQueue implements Queue {

	LinkedBlockingDeque<WorkChannel> lazy;
	
	private ServerState state;
	
	private TaskHandler clientReqHandler = null;
	
	private int processed = 0;
	
	private static Logger logger = LoggerFactory.getLogger("lazy queue");
	
	public LazyQueue(ServerState state) {
		this.state = state;
		lazy = new LinkedBlockingDeque<WorkChannel>();
		
	}
	
	@Override
	public void put(WorkMessage work, Channel channel) {
		lazy.add(new WorkChannel(work, channel));
	}

	@Override
	public WorkChannel get() {
		try {
			return lazy.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSize() {
		return lazy.size();
	}
	
	
	public WorkMessage getTask(){
		WorkChannel tc= get();
		if(tc != null ){
			return tc.getWorkMessage();
		}
		return null;
	}
	
	public boolean process(){
		if(lazy.size() == 0) {
			logger.info("lazy queue size is 0, process other tasks ?");
			return false;
		}
		WorkChannel t = get();
		WorkMessage work = t.getWorkMessage();
		
		clientReqHandler = new TaskHandler(state);
		
		ByteString data = work.getTask().getCommandMessage().getReqMsg().getData(); 
		WorkMessage.Builder duplicate = WorkMessage.newBuilder(work);
		//Perform the processing for client request here
		logger.info("Initiating processing for inbound message");
		handleClientOperation(work, t.getChannel());
		TaskType type = work.getTask().getType();
		
		logger.info("processing lazy task +++ " +work);
		processed ++;
		// Get data while replicating it and send it to this method
		state.getEmon().updateAndBoradCast(work.getTask(),data);
		
		return true;
	}

	@Override
	public WorkMessage rebalance() {
		logger.error("operation not supported in this type of queue");
		return null;
	}
	
	@Override
	public int numEnqueued() {
		// TODO Auto-generated method stub
		return lazy.size();
	}

	@Override
	public int numProcessed() {
		// TODO Auto-generated method stub
		return processed;
	}

	public void handleClientOperation(WorkMessage workMessage, Channel channel){
		Node origin = workMessage.getHeader().getOrigin();
		EdgeInfo ei = ResourceUtil.nodeToEdge(origin);
		Channel destination = ResourceUtil.getChannel(ei);
		clientReqHandler.handleTask(workMessage, channel);
		
	}
}
