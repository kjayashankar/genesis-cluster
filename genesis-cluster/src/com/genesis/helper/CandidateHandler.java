package com.genesis.helper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeInfo;

import io.netty.channel.Channel;
import pipe.common.Common.Node;
import pipe.election.Election.LeaderStatus.LeaderQuery;
import pipe.work.Work.Vote;
import pipe.work.Work.WorkMessage;


public class CandidateHandler extends ParentHandler  {

	private static Logger logger = LoggerFactory.getLogger("candidate handler"); 
	
	public CandidateHandler(ServerState state) {
		super(state);
	}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		if(msg.hasBeat())
			handleBeat(msg, channel);
		
		else if(msg.hasLeader()) {
			handleLeader(msg,channel);
		}
		else if(msg.hasVerdict()){
			Vote vote = msg.getVerdict();
			logger.info("received a vote from "+msg.getHeader().getOrigin());
			state.getEmon().handleVote(msg);
		}
	}

	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
	}
	private void handleLeader(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		if(msg.getLeader().getAction() == LeaderQuery.THELEADERIS) {
			Node origin = msg.getHeader().getOrigin();
			EdgeInfo leader = new EdgeInfo(origin.getId(),origin.getHost(),origin.getPort());
			leader.status = "ALIVE";
			state.getEmon().setLeader(leader);
			state.getEmon().passMsg(msg);
			logger.info("leader received, updated leader : "+leader.getRef());
		}
	}
}