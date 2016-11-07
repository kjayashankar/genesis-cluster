package com.genesis.monitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeMonitor;

public class StealerThread extends Thread{

	private static Logger logger = LoggerFactory.getLogger("stealer");
	private ServerState state;
	private EdgeMonitor edgeMon;
	
	public StealerThread(ServerState state) {
		this.state = state;
		this.edgeMon = state.getEmon();
	}
	
	
	public void run(){
		while(1 == 1){
			state.getEmon().handleStealer();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
