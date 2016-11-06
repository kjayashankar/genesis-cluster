package com.genesis.helper;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.monitors.NetworkMonitor;
import com.genesis.router.server.ServerState;

import io.netty.channel.Channel;
import pipe.common.Common.Node;
import pipe.work.Work.DragonBeat;
import pipe.work.Work.NodeLinks;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

public class TaskHandler extends ParentHandler {

	private static Logger logger = LoggerFactory.getLogger("leader handler");
	ServerState state;
	TaskHandlerHelper taskHelper;
	
	public TaskHandler(ServerState state) {
		super(state);
		this.state = state;
		taskHelper = new TaskHandlerHelper(state);
	}


	@Override
	public void handleTask(WorkMessage msg, Channel channel) {
		if(msg.hasTask()){
			Task task = msg.getTask();
			CommandMessage cmdMsg = task.getCommandMessage();
			logger.info("Processing command message, contained message status is "+ cmdMsg.hasReqMsg());
				if(cmdMsg.hasReqMsg()){
					taskHelper.handleMessage( cmdMsg,channel) ;
				} 
		}
	}

	@Override
	public void handleBeat(WorkMessage msg, Channel channel) {}

	@Override
	public void handleMessage(WorkMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		if(msg.hasTask()){
			handleTask(msg,channel);
		}
		
	}
	
	public void handleRegister(WorkMessage msg, Channel channel) {}


	@Override
	public void handleDragonL1(WorkMessage msg, Channel channel) {}

	@Override
	public void handleState(WorkMessage msg, Channel channel) {
		
	}

	@Override
	public void handleVote(WorkMessage msg, Channel channel) {
		
	}
}
