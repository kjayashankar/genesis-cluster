package com.genesis.resource;

import java.util.List;

import com.genesis.router.server.edges.EdgeInfo;

import pipe.common.Common.Header;
import pipe.common.Common.Node;
import pipe.election.Election.LeaderStatus;
import pipe.election.Election.LeaderStatus.LeaderQuery;
import pipe.election.Election.LeaderStatus.LeaderState;
import pipe.work.Work.DragonBeat;
import pipe.work.Work.Heartbeat;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.Register;
import pipe.work.Work.Vote;
import pipe.work.Work.Vote.Verdict;
import pipe.work.Work.WorkMessage;
import pipe.work.Work.WorkState;

public class ResourceUtil {

	
	public static WorkMessage createHB(EdgeInfo ei,WorkState sb,int destID) {
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Heartbeat.Builder bb = Heartbeat.newBuilder();
		bb.setState(sb);

		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());

		WorkMessage.Builder wb = WorkMessage.newBuilder();
		wb.setHeader(hb);
		wb.setSecret(1001);
		wb.setBeat(bb);

		return wb.build();
	}

	public static WorkMessage createDB(String mode,EdgeInfo ei, int destID , NodeLinks links , int outCheckSum) {

		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
		
		WorkMessage.Builder wb = WorkMessage.newBuilder();
		DragonBeat.Builder dragon = DragonBeat.newBuilder();
		dragon.addNodelinks(links);
		dragon.setChecksum(outCheckSum);
		dragon.setMode(mode);
		wb.setSecret(1001);
		wb.setHeader(hb);
		wb.setDragon(dragon);
		
		return wb.build();
	}

	public static WorkMessage createDBList(String mode,EdgeInfo ei, int destID, 
		
		List<NodeLinks> links, int outCheckSum) {
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
		
		WorkMessage.Builder wb = WorkMessage.newBuilder();
		DragonBeat.Builder dragon = DragonBeat.newBuilder();
		dragon.addAllNodelinks(links);
		dragon.setChecksum(outCheckSum);
		dragon.setMode(mode);
		wb.setSecret(1001);
		wb.setHeader(hb);
		wb.setDragon(dragon);
		return wb.build();
	}

	public static WorkMessage createRegisterMsg(EdgeInfo ei, int destID) {
		
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
		
		WorkMessage.Builder wb = WorkMessage.newBuilder();
		
		wb.setSecret(1001);
		wb.setHeader(hb);

		Register.Builder rb = Register.newBuilder();
		rb.setMode("NEWBIE");
		wb.setRegister(rb);
		
		return wb.build();
	}

	public static WorkMessage createNewbieMessage(EdgeInfo ei, int destID, Node newbie) {
		
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
		
		WorkMessage.Builder wb = WorkMessage.newBuilder();
		
		wb.setSecret(1001);
		wb.setHeader(hb);

		Register.Builder rb = Register.newBuilder();
		rb.setMode("NEWBIE");
		rb.setDestNode(newbie);
		wb.setRegister(rb);
		
		return wb.build();
	}
	
	public static EdgeInfo nodeToEdge (Node node){
		return new EdgeInfo(node.getId(),node.getHost(),node.getPort());
		
	}

	public static LeaderStatus createElectionMessage(EdgeInfo thisNode) {
		
		LeaderStatus.Builder leader = LeaderStatus.newBuilder();
		leader.setAction(LeaderQuery.WHOISTHELEADER);
		leader.setState(LeaderState.LEADERDEAD);
		
		leader.setLeaderHost(thisNode.getHost());
		leader.setLeaderId(thisNode.getRef());
		leader.setLeaderPort(thisNode.getPort());
		
		return leader.build();
	}

	public static WorkMessage createElectionMsg(EdgeInfo ei, int destID, LeaderStatus lStatus) {
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		Header.Builder header = Header.newBuilder();
		
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
				
		wm.setSecret(1001);
		wm.setHeader(hb);
		wm.setLeader(lStatus);
		
		return wm.build();		
	}

	public static WorkMessage createVoteMessage(EdgeInfo ei, int destID, Verdict vote) {
		WorkMessage.Builder wm = WorkMessage.newBuilder();
		Header.Builder header = Header.newBuilder();
		
		Node.Builder nb = Node.newBuilder();
		nb.setId(ei.getRef());
		nb.setHost(ei.getHost());
		nb.setPort(ei.getPort());
		
		Header.Builder hb = Header.newBuilder();
		hb.setOrigin(nb);
		hb.setDestination(destID);
		hb.setTime(System.currentTimeMillis());
				
		wm.setSecret(1001);
		wm.setHeader(hb);
		
		Vote.Builder voB = Vote.newBuilder();
		voB.setVerdict(vote);
		wm.setVerdict(voB);
		
		return wm.build();		
	}
}
