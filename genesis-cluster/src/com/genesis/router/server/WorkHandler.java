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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.helper.HandlerUtils;
import com.genesis.helper.ServerHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.work.Work.WorkMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class WorkHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected static Logger logger = LoggerFactory.getLogger("work");
	public ServerState state;
	private ServerHandler handler;
	protected boolean debug = false;
	HandlerUtils hUtils;


	public WorkHandler(){
		
	}
	public WorkHandler(ServerState state) {
		if (state != null) {
			this.state = state;
			hUtils = new HandlerUtils(state);
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	public void handleMessage(WorkMessage msg, Channel channel) {
		if (msg == null) {
			logger.error("ERROR: Unexpected content - " + msg);
			return;
		}
		logger.info("received msg on "+state.getConf().getNodeId()+" and state: "+state.state);
		handler = hUtils.getHandlerInstance();
		handler.handleMessage(msg, channel);	
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
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}