/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.genesis.router.server.tasks;

import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pipe.work.Work.Task;

/**
 * Processing of tasks
 * 
 * @author gash
 *
 */
public class TaskList {
	protected static Logger logger = LoggerFactory.getLogger("work");

	private LinkedBlockingDeque<Task> inbound;
	private LinkedBlockingDeque<Task> outbound;
	private int processed;
	private int balanced;
	private Rebalancer rebalance;
	public final int MAX_SIZE = 4;
	public final int STEALING_THRESHOLD = 1;
	
	
	public TaskList(Rebalancer rb) {
		rebalance = rb;
		inbound = new LinkedBlockingDeque<Task>();
		
		for(int i=0; i<20;i++){
			inbound.add(addDummyTasks());
		}
	}

	public void addTask(Task t) {
		inbound.add(t);
	}

	public int numEnqueued() {
		return inbound.size();
	}

	public int numProcessed() {
		return processed;
	}

	public int numBalanced() {
		return balanced;
	}
	
	/**
	 * If the thread has less no. of inbound threads it starts stealing from adjacent nodes. 
	 * @return boolean value
	 */
	public boolean startStealing(){
		//logger.info("Steal flag is :: "+ (numEnqueued() <= STEALING_THRESHOLD));
		return numEnqueued() >= STEALING_THRESHOLD ? true: false;
	}

	/**
	 * Task will be taken to be given to some other node, 
	 * Implements sharing of resources among the cluster
	 * it measures if the queue no. of tasks enqueued are more than 50% of the allowed size
	 * take the task n give it to the adjacent node.
	 * 
	 * @return Task to be assigned to the other node now
	 */
	public Task rebalance() {
		Task t = null;

		try {
			if (rebalance != null && !rebalance.allow())
				return t;

			t = inbound.take();
			balanced++;
		} catch (InterruptedException e) {
			logger.error("failed to rebalance a task", e);
		}
		return t;
	}

	/**
	 * task taken to be processed by this node
	 * 
	 * @return
	 */
	public Task dequeue() {
		Task t = null;
		try {
			t = inbound.take();
			processed++;
		} catch (InterruptedException e) {
			logger.error("failed to dequeue a task", e);
		}
		return t;
	}
	
	public Task addDummyTasks(){
		Task.Builder t = Task.newBuilder();
		t.setSeqId(-1);
		t.setSeriesId(-1);
		//t.setCommandMessage(null);
		
		return t.buildPartial();
		
	}
}
