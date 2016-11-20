package com.genesis.router.server.edges;

import com.genesis.router.server.ServerState;

public class GlobalEdgeMonitor {

	private ServerState state;
	private EdgeList globalOutboud = new EdgeList();	
	
	GlobalEdgeMonitor(ServerState state) {
		this.state = state;
	}
	
}
