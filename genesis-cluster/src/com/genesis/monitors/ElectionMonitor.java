package com.genesis.monitors;

import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.edges.EdgeInfo;

import pipe.election.Election.LeaderStatus;


public class ElectionMonitor {

	private int votedNum = 0;
		
	public LeaderStatus init(EdgeInfo thisNode) {
		return ResourceUtil.createElectionMessage(thisNode);
	}
	
	public void setVotedNum(int votedNum){
		this.votedNum  += votedNum;
	}
	
	public int getVoted(){
		return votedNum;
	}
	
}
