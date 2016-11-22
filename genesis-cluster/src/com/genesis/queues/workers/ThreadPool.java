package com.genesis.queues.workers;

import java.util.ArrayList;
import java.util.List;

import com.genesis.router.server.ServerState;

public class ThreadPool {

	private List<Worker> workerGroup;
	private long waitSeconds; 
	private int size;
	private int workerIndex = 0;
	private ServerState state;
	
	public ThreadPool(int size,ServerState state){
		this.size = size;
		this.state = state;
		workerGroup = new ArrayList<Worker>(size);
	}

	public void init(){
		for(int index = 0 ; index < size ; index ++) {
			Worker threadWorker = new Worker(state,waitSeconds,index);
			workerGroup.add(threadWorker);
		}
	}
	
	public void startWorkers(){
		Thread[] thread = new Thread[size];
		for(int index = 0 ; index < size ; index ++){
			thread[index] = new Thread(workerGroup.get(index));
			thread[index].start();
		}
	}
	
	
	public void setThreadPause(long seconds){
		this.waitSeconds = seconds;
	}
	
	public Worker getWorker(){
		if(workerIndex == size){
			workerIndex = 0;
		}
		Worker worker = workerGroup.get(workerIndex);
		workerIndex++;
		return worker;
	}
	
	
}
