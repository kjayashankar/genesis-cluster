package com.genesis.helper;

import com.genesis.router.server.ServerState;

public class HandlerUtils {

	ServerState state ;

	public HandlerUtils(ServerState state){
		this.state = state;
	}
	
	
	public ServerHandler getHandlerInstance(){
		
		switch(state.state){
		
			case ORPHAN:
				return new OrphanHandler(state);
				
			case LEADER:
				return new LeaderHandler(state);
				
			case FOLLOWER:
				return new FollowerHandler(state);
				
			case CANDIDATE:
				return new CandidateHandler(state);
				
			case VOTED:
				return new VotedHandler(state);
				
				
		}
		return new OrphanHandler(state);
	}
}
