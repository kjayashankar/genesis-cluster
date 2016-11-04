package com.genesis.monitors;

import com.genesis.router.server.ServerState;
import com.genesis.router.server.edges.EdgeMonitor;

public class StealerThread extends Thread{

	private ServerState state;
	private EdgeMonitor edgeMon;
	
	public StealerThread(ServerState state) {
		this.state = state;
		this.edgeMon = state.getEmon();
	}
	
	
	public void run(){
		while(1==1){
			state.getEmon().handleStealer();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
