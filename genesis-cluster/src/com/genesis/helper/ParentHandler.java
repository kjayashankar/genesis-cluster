package com.genesis.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.monitors.NetworkMonitor;
import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeInfo;

import io.netty.channel.Channel;
import pipe.common.Common.Failure;
import pipe.common.Common.Node;
import pipe.work.Work.DragonBeat;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.TaskType;
import pipe.work.Work.WorkMessage;

public class ParentHandler implements ServerHandler{

	private static Logger logger = LoggerFactory.getLogger("parent handler");
	protected ServerState state;
	
	public ParentHandler(ServerState state) {
		this.state = state;
	}
	
	@Override
	public void handleTask(WorkMessage msg, Channel channel) {
		
		if(msg.getTask().getType() == TaskType.LAZYTASK){
			state.getQueueMonitor().getLazyQueue().put(msg, null);
		}
	}

	@Override
	public void handleBeat(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		logger.info("heart beat received :" +msg);
		state.getEmon().updateHeartBeat(msg.getHeader().getOrigin(),
				msg.getHeader().getTime(),msg.getState());
	}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	public void handleDragonL2(WorkMessage msg, Channel channel) {
		logger.debug("Dragon L2 "+msg);
		NetworkMonitor nmon = NetworkMonitor.getInstance();
		DragonBeat dragon = msg.getDragon(); 
		List<NodeLinks> links = dragon.getNodelinksList();
		nmon.nmap = links;
		state.getEmon().passOnDragon("L2",links,nmon.getOutCheckSum());
	}
	
	public void handleDragonL1(WorkMessage msg, Channel channel) {

		NetworkMonitor nmon = NetworkMonitor.getInstance();
		DragonBeat dragon = msg.getDragon();	 
		List<NodeLinks> links = new ArrayList<NodeLinks>();
		links.addAll(dragon.getNodelinksList());
		logger.debug("L1 links received : "+links);
		links.add(state.getEmon().prepareDragonBeatMsg());
		logger.debug("L1 links received and transmitted : "+links);	
		state.getEmon().passOnDragon("L1",links,nmon.getOutCheckSum());
	}
	
	public WorkMessage handleSteal(WorkMessage wm){
		logger.info("received a steal request from node "+wm.getHeader().getNodeId());
		WorkMessage returnWorkMessage = state.getQueueMonitor().getInboundQueue().rebalance();
		if(returnWorkMessage != null){
			WorkMessage.Builder tempWork = WorkMessage.newBuilder(returnWorkMessage);
			tempWork.setStealResponse(true);
			logger.info("gave my task");
			return tempWork.build();
		}
		else{
			logger.error("no tasks to give it to steal!");
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(wm.getHeader().getNodeId());
			eb.setMessage("No stealing needed");
			WorkMessage.Builder rb = WorkMessage.newBuilder(wm);
			rb.setErr(eb);
			return rb.build();
		}
	}
	
	public void handleStealResponse(WorkMessage wm, Channel channel) {
		logger.info("got a steal response task");
		state.getQueueMonitor().getInboundQueue().put(wm, channel);
		logger.info("added to my inbound queue");
	}
	
	public void handleModerator(WorkMessage wm, Channel channel) {
		state.getEmon().handleModerator(wm);
	}
	
}
