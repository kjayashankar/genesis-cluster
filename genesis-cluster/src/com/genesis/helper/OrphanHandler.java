package com.genesis.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeInfo;

import io.netty.channel.Channel;
import pipe.common.Common.Node;
import pipe.work.Work.WorkMessage;

public class OrphanHandler extends ParentHandler {

	private static Logger logger = LoggerFactory.getLogger("orphan handler");
	
	
	public OrphanHandler(ServerState state) {
		super(state);
	}


	@Override
	public void handleTask(WorkMessage msg, Channel channel) {
		
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
		else if(msg.hasRegister())
			handleRegister(msg,channel);
		else if(msg.hasLeader()){
			handleLeader(msg, channel);
		}
	}

	private void handleRegister(WorkMessage msg, Channel channel) {
		logger.info("registered node!");
		if(msg.getRegister() != null ){
			if(msg.getRegister().hasLeader()){
				state.getEmon().setLeader(
						ResourceUtil.nodeToEdge(msg.getRegister().getLeader()));
			}
		}
		
	}


	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}
	protected void handleLeader(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		switch(msg.getLeader().getAction()){
			case THELEADERIS: {
			
				Node origin = msg.getHeader().getOrigin();
				EdgeInfo leader = new EdgeInfo(origin.getId(),origin.getHost(),origin.getPort());
				leader.status = "ALIVE";
				state.getEmon().setLeader(leader);
				state.getEmon().passMsg(msg);
				logger.info("leader received, updated leader : "+leader.getRef());
				state.state = STATE.FOLLOWER;
				break;
			}
			case WHOISTHELEADER: {
				switch(msg.getLeader().getState()){
					case LEADERDEAD: {
						if(state.state != STATE.CANDIDATE && state.state != STATE.VOTED){
							state.state = STATE.VOTED;
							
							state.getEmon().handleElectionMessage(msg);
						}
						break;
					}
					default:{
						// probably a new node, help it to find leader
						//WorkMessage workMessage = state.getEmon().helpFindLeaderNode(msg);
						//if(workMessage != null)
						//	channel.writeAndFlush(workMessage);
					}
				
				}
			}
		}
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
	}
}