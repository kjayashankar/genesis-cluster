package com.genesis.helper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.STATE;
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

	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
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
	
}