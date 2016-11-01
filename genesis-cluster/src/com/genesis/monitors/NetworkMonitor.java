package com.genesis.monitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;

import pipe.common.Common.Node;
import pipe.work.Work.NodeLinks;

public class NetworkMonitor implements Runnable{

	private static Logger logger = LoggerFactory.getLogger("network monitor");
	private static NetworkMonitor netmon ;
	public List<NodeLinks> nmap = new
			ArrayList<NodeLinks>();
	public Set<Integer> nodes = new TreeSet<Integer>();
	private ServerState state = null;
	private int inCheckSum = -1;
	private boolean modifiedNodes = false;
	private boolean forever =true;
	private int outCheckSum = -1;
	int waitCycle = 1;
	
	public void notifyMonitor(){
		
	}
	
	public int getOutCheckSum() {
		return outCheckSum;
	}

	public void setOutCheckSum(int outCheckSum) {
		this.outCheckSum = outCheckSum;
	}

	
	public void setState(ServerState state){
		this.state = state;
	}
	private int generateNewCheckSum(){
		int temp = 123677;
		return temp;
	}
	
	public boolean verifyInCheck(int incheck) {
		return incheck == inCheckSum;
	}
	public synchronized void updateNodes(List<NodeLinks> nodeLink, int checkSum){
		//logger.info("before update "+nodes +"\n"+nmap);

		if(nodeLink.size() == 0){
			nmap = new
					ArrayList<NodeLinks>();
			nodes = new TreeSet<Integer>();
		}
		else{
			for(NodeLinks link:nodeLink) {
				int ref = link.getMe().getId();
				if(!nodes.contains(ref)){
					nodes.add(ref);
					this.nmap.add(link);
				}
			}
		}
		//logger.info("after update "+nodes +"\n"+nmap);

		//this.nmap.addAll(nodeLink);
		//inCheckSum = checkSum;
		//generateNewCheckSum();
	}
	
	private List<NodeLinks> sortNodes(List<NodeLinks> nodeLink) {
		// TODO Auto-generated method stub
		return null;
	}

	private NetworkMonitor(){
		
	}
	
	public static NetworkMonitor getInstance(){
		if(netmon == null){
			netmon = new NetworkMonitor();
		}
		return netmon;	
	}
	
	public void run(){
		while(forever) {
			if(state != null) {
				if(state.state == STATE.LEADER && ++waitCycle % 3 == 0 ){
					logger.info("initiate dragon beat");
					if(outCheckSum == -1)
						outCheckSum = generateNewCheckSum();
					state.getEmon().initDragonBeat(outCheckSum);
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

	public void registerNewOutBound(Node newbie) {
		/*List<NodeLinks> tempLinks = new ArrayList<NodeLinks>();
		
		if(nmap != null && nmap.size() > 0){
			tempLinks = nmap;
		}
		else if(nmapOut != null && nmapOut.size() > 0){
			tempLinks = nmapOut;
		}
		for(NodeLinks nLink : tempLinks){
			
		}*/
		
		state.getEmon().registerNewbie(newbie);
		
	}
	
	
}
