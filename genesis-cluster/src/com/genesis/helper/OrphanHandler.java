package com.genesis.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;

import io.netty.channel.Channel;
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
		
	}


	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
	}
}