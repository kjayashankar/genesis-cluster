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
package com.genesis.router.server;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.db.handlers.DBUtils;
import com.genesis.queues.Queue;
import com.genesis.resource.ResourceUtil;
import com.message.ClientMessage.Operation;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Node;
import pipe.work.Work.Task;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class CommandHandler extends SimpleChannelInboundHandler<CommandMessage> {
	protected static Logger logger = LoggerFactory.getLogger("CommandHandler");
	//protected RoutingConf conf;
	protected ServerState state;
	private Queue inboundQueue;
	//private QueueManager queues;

	public CommandHandler(ServerState state) {
		if (state != null) {
			this.state = state;
			inboundQueue = state.getQueueMonitor().getInboundQueue();
		}
	}

	/**
	 * override this method to provide processing behavior. This implementation
	 * mimics the routing we see in annotating classes to support a RESTful-like
	 * behavior (e.g., jax-rs).
	 * 
	 * @param msg
	 */
	public void handleMessage(CommandMessage msg, Channel channel) {
		if (msg == null) {
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}
		
		if(msg.hasReqMsg() && msg.getReqMsg().getOperation() == Operation.POST){
			Task.Builder myTask = Task.newBuilder();
			myTask.setCommandMessage(msg);
			myTask.setSeqId(myTask.getSeqId());
			myTask.setSeriesId(myTask.getSeriesId());

			WorkMessage workMessage = ResourceUtil.buildWorkMessageFromTask(myTask.build(), state);
			inboundQueue.put(workMessage, channel);
			
			logger.info("Client Message added to the Inbound Queue.");
			return;
		}
		// it's a get
		else if(msg.hasReqMsg()){
			//create a task of it and submit to the inbound along with the channel
			
			String fileName = msg.getReqMsg().getKey();
			if(DBUtils.isfileExist(fileName)){
				Task.Builder myTask = Task.newBuilder();
				myTask.setCommandMessage(msg);
				myTask.setSeqId(myTask.getSeqId());
				myTask.setSeriesId(myTask.getSeriesId());
	
				WorkMessage workMessage = ResourceUtil.buildWorkMessageFromTask(myTask.build(), state);
				inboundQueue.put(workMessage, channel);
				
				logger.info("Client Message added to the Inbound Queue.");
			}
			else {
				String id = UUID.randomUUID().toString();
				state.getgMon().forwardRequest(id,msg);
				Node origin = msg.getHeader().getOrigin();
				state.moderator.put(id, channel);
				state.getEmon().updateMooderator(id,origin);
			}
		}
	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}