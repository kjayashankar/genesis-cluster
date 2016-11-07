package com.genesis.router.server.tasks;

import com.genesis.queues.Queue;

public class SimpleBalancer implements Rebalancer {

	private Queue inbound;
	
	public SimpleBalancer() {}


	public void setQueue(Queue inbound) {
		this.inbound = inbound;
	}


	@Override
	public boolean allow() {
		return calcLoad() > 0.50;
	}

	@Override
	public float calcLoad() {
		return inbound.numEnqueued() / 200;
	}

}
