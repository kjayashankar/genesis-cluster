package com.genesis.router.server.tasks;


public class SimpleBalancer implements Rebalancer {

	private TaskList taskList;
	
	public SimpleBalancer() {}


	public void setTaskList(TaskList taskList) {
		this.taskList = taskList;
	}


	@Override
	public boolean allow() {
		return calcLoad() > 0.50;
	}

	@Override
	public float calcLoad() {
		return taskList.numEnqueued() / taskList.MAX_SIZE;
	}

}
