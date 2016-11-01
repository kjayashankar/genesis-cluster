package com.genesis.monitors;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;

import pipe.common.Common.Header;
import pipe.common.Common.Node;
import pipe.election.Election.LeaderStatus;
import pipe.election.Election.LeaderStatus.LeaderQuery;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.WorkMessage;

public class DragonThread implements Runnable{

	private static Logger logger = LoggerFactory.getLogger("Dragon mon");
	private ServerState state;
	private boolean initialized = false;

	public NetworkMonitor nmon = NetworkMonitor.getInstance();
	int waitCycle = 0;
	
	private WorkMessage leader;
	
	public void init(){
		
		WorkMessage.Builder leaderB = WorkMessage.newBuilder();
		LeaderStatus.Builder status = LeaderStatus.newBuilder();
		status.setAction(LeaderQuery.THELEADERIS);
		leaderB.setLeader(status.build());
		Header.Builder header = Header.newBuilder();
		Node.Builder origin = Node.newBuilder();
		origin.setId(state.getConf().getNodeId());
		origin.setHost(state.getConf().getHost());
		origin.setPort(state.getConf().getWorkPort());
		header.setOrigin(origin.build());
		header.setTime(System.currentTimeMillis());
		leaderB.setHeader(header);
		leaderB.setSecret(1002);
		leader = leaderB.build();
		initialized = true;
	}
	
	public void run(){
		while(1==1){
			if(state != null) {
				if(!initialized)
					init();
				if(state.state == STATE.LEADER && ++waitCycle % 3 == 1){
					state.getEmon().passMsg(leader);
				}
				if(state.state == STATE.LEADER && ++waitCycle % 3 == 2){
					logger.info("initiate dragon beat level 2");
					if(nmon.nmap != null && nmon.nmap.size() > 0 ){
						List<NodeLinks> nmapOut = nmon.nmap;
						nmon.updateNodes( new ArrayList<NodeLinks>(), 0);
						List<NodeLinks> nodes = nmapOut;
						//logger.info("ready to send dragon L2 " + nodes.toString());
						state.getEmon().passOnDragon("L2", nodes, 0);
					}
					else{
						logger.error("invalid nmon size in Dragon Thread");
					}
				}
			}
			try{
				Thread.sleep(4000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}



	public void setState(ServerState state) {
		this.state = state;
	}

}
