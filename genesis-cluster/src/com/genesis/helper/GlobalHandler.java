package com.genesis.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.ServerState;

import global.Global.GlobalMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import routing.Pipe.CommandMessage;

public class GlobalHandler extends SimpleChannelInboundHandler<GlobalMessage> {

	protected static Logger logger = LoggerFactory.getLogger("Global Handler");
	//protected RoutingConf conf;
	protected ServerState state;
	//private GlobalQueue globalInbound;
	//private GlobalQueue globalOutbound;

	//private QueueManager queues;

	public GlobalHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, GlobalMessage arg1) throws Exception {
		logger.info("received global message");
		
	}
	
	protected void handleMessage(GlobalMessage msg,Channel channel){
		if(msg.hasResponse()){
			if(state.getGlobalConf().getClusterId() == msg.getGlobalHeader().getDestinationId()){
				// my cluster request;
				String uuid = msg.getResponse().getRequestId();
				// check if i have the channel
				if(state.moderator.containsKey(uuid)){
					Channel chn = state.moderator.get(uuid);
				//convert into CommandRespnse Message
					if(chn != null ){
						CommandMessage response =
						ResourceUtil.convertIntoCommand(msg);
						chn.writeAndFlush(response);
					}
					else {
						//pass into loop
					}
				}
				// else give it to ring
			}
			else{
				//may be in other cluster?
				state.getgMon().pushMessagesIntoCluster(msg);
			}
		}
	}

	
	
}
