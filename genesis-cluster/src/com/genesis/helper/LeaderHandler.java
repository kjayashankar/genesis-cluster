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
import pipe.common.Common.Node;
import pipe.work.Work.DragonBeat;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.WorkMessage;

public class LeaderHandler extends ParentHandler {

	private static Logger logger = LoggerFactory.getLogger("leader handler");

	
	public LeaderHandler(ServerState state) {
		super(state);
	}

	@Override
	public void handleBeat(WorkMessage msg, Channel channel) {	
		state.getEmon().updateHeartBeat(msg.getHeader().getOrigin(),
				msg.getHeader().getTime(),msg.getState());
	}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		if(msg.hasBeat())
			handleBeat(msg, channel);
		else if(msg.hasDragon()){
			if("L1".equalsIgnoreCase(msg.getDragon().getMode()))
				handleDragonL1(msg,channel);
		}
		else if(msg.hasRegister()){
			handleRegister(msg,channel);
		}
		else if(msg.hasLeader()){
			handleLeader(msg, channel);
		}
		else if(msg.hasSteal()){
			WorkMessage stolenTask = handleSteal(msg);
			channel.writeAndFlush(stolenTask);
		}
		else if(msg.hasStealResponse()){
			handleStealResponse(msg, channel);
		}
		else if(msg.hasTask()){
			handleTask(msg,channel);
		}
		
	}
	
	public void handleRegister(WorkMessage msg, Channel channel) {
		Node newbie = msg.getHeader().getOrigin();
		NetworkMonitor nmon = NetworkMonitor.getInstance();
		nmon.registerNewOutBound(newbie);
		WorkMessage wm = state.getEmon().helpFindLeaderNode(msg);
		if(wm != null)
			channel.writeAndFlush(wm);
	}
	protected void handleLeader(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		switch(msg.getLeader().getAction()){
			
			case WHOISTHELEADER: {
				switch(msg.getLeader().getState()){
					case LEADERDEAD: {
						state.getEmon().claimLeadership();
						return;
					}				
				}
			}
		}
	}


	@Override
	public void handleDragonL1(WorkMessage msg, Channel channel) {
		NetworkMonitor nmon = NetworkMonitor.getInstance();
		DragonBeat dragon = msg.getDragon();
		List<NodeLinks> links = new ArrayList<NodeLinks>();
		//logger.info("L1 links received : "+dragon.getNodelinksList());
		links.addAll(dragon.getNodelinksList());
		nmon.updateNodes(links, 0);
		//state.getEmon().passOnDragon("L2", links, nmon.getOutCheckSum());
	}

	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
	}
}