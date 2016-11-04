package com.genesis.queues;

import java.util.concurrent.LinkedBlockingDeque;

import io.netty.channel.Channel;
import pipe.work.Work.Task;

public class LazyQueue implements Queue {

	LinkedBlockingDeque<TaskChannel> lazy;
	
	public LazyQueue() {
		lazy = new LinkedBlockingDeque<TaskChannel>();
		
	}
	
	@Override
	public void put(Task task, Channel channel) {
		// TODO Auto-generated method stub
		lazy.add(e)
	}

	@Override
	public TaskChannel get() {
		// TODO Auto-generated method stub
		try {
			return lazy.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return lazy.size();
	}
	
	public void put(Task t){
		put(t,null);
	}
	
	public Task getTask(){
		TaskChannel tc= get();
		if(tc != null ){
			return tc.getTask();
		}
		return null;
	}

}
