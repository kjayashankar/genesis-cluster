package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.helper.TaskHandler;
import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeInfo;
import com.genesis.router.server.tasks.Rebalancer;
import com.message.ClientMessage.Operation;

import io.netty.channel.Channel;
import pipe.common.Common.Header;
import pipe.common.Common.Node;
import pipe.work.Work.Task;
import pipe.work.Work.TaskType;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

public class InboundQueue implements Queue{

	LinkedBlockingDeque<WorkChannel> inbound;
	
	private int balanced;
	private int processed = 0;
	private ServerState state;
	Rebalancer rebalance ;
	private TaskHandler clientReqHandler;
	private static Logger logger = LoggerFactory.getLogger("inbound queue");
	private boolean debug = false;
	
	
	public InboundQueue(ServerState state,Rebalancer newBalancer) {
		this.state = state;
		this.rebalance = newBalancer;
		rebalance.setQueue(this);
		inbound = new LinkedBlockingDeque<WorkChannel>();
		if(debug)
			addDummy();
	}
	
	private void addDummy() {
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		Header.Builder header = Header.newBuilder();
		wm.setSecret(1);
		wm.setHeader(header);
		Task.Builder t = Task.newBuilder();
		t.setSeqId(12);
		t.setSeriesId(1000);
		wm.setTask(t);
		for(int i = 0; i < 20 ; i ++){
			inbound.add(new WorkChannel(wm.build(),null));
		}
		
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
			//logger.info("inbound queue size is 0, process other queues, may be lazy ?");
			return false;
		}
		
		clientReqHandler = new TaskHandler(state);
		
		WorkChannel t = get();
		WorkMessage work = t.getWorkMessage();
		
		//Perform the processing for client request here
		logger.info("Initiating processing for inbound message");
		handleClientOperation(work, t.getChannel());
		TaskType type = work.getTask().getType();
		// Into Lazy queue for the first time
		if((type == null || type == TaskType.SIMPLETASK) && isEligible(work))
			state.getEmon().sendToLazyQueue(work.getTask());
		// Already a lazy task, no need to check eligibility, just update header and broadcast
		
		processed ++;
		return true;
	}
	
	private boolean isEligible(WorkMessage work) {
		CommandMessage cmd = work.getTask().getCommandMessage();
		Operation op = cmd.getReqMsg().getOperation() ;
		if(op == Operation.POST || op == Operation.PUT)
				return true;
		return false;
	}
	
	/**
	 * Invokes the handler to perform client requested operation
	 * @return 
	 */
	public void handleClientOperation(WorkMessage workMessage, Channel channel){
		Node origin = workMessage.getHeader().getOrigin();
		EdgeInfo ei = ResourceUtil.nodeToEdge(origin);
		Channel destination = ResourceUtil.getChannel(ei);
		clientReqHandler.handleTask(workMessage, channel);
		
	}
	
	public WorkMessage rebalance() {
		WorkChannel t = null;

		try {
			//if (rebalance != null && !rebalance.allow()) {
			if(inbound != null && inbound.size() > 0)
				t = inbound.take();
				balanced++;
			//}
		} catch (InterruptedException e) {
			logger.error("failed to rebalance a task", e);
		}
		if(t != null)
			return t.getWorkMessage();
		return null;
	}

	@Override
	public int numEnqueued() {
		// TODO Auto-generated method stub
		return inbound.size();
	}

	@Override
	public int numProcessed() {
		// TODO Auto-generated method stub
		return processed;
	}

}
