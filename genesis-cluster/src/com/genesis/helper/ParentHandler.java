package com.genesis.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.monitors.NetworkMonitor;
import com.genesis.router.server.ServerState;

import io.netty.channel.Channel;
import pipe.work.Work.DragonBeat;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.WorkMessage;

public class ParentHandler implements ServerHandler{

	private static Logger logger = LoggerFactory.getLogger("parent handler");
	protected ServerState state;
	
	public ParentHandler(ServerState state) {
		this.state = state;
	}
	
	@Override
	public void handleTask(WorkMessage msg, Channel channel) {
		
		
	}

	@Override
	public void handleBeat(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		state.getEmon().updateHeartBeat(msg.getHeader().getOrigin(),
				msg.getHeader().getTime(),msg.getState());
	}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		if(msg.hasTask()){
			handleTask(msg,channel);
		}
		
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
	
	
}
