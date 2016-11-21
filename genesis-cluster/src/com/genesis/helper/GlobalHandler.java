package com.genesis.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.db.handlers.DBUtils;
import com.genesis.db.handlers.RedisDBServiceImpl;
import com.genesis.db.service.IDBService;
import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.ServerState;
import com.google.protobuf.ByteString;
import com.message.ClientMessage.RequestMessage;
import com.message.ClientMessage.ResponseMessage;

import global.Global.File;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import global.Global.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Header;
import routing.Pipe.CommandMessage;

public class GlobalHandler extends SimpleChannelInboundHandler<GlobalMessage> {

	protected static Logger logger = LoggerFactory.getLogger("Global Handler");
	//protected RoutingConf conf;
	protected ServerState state;
	IDBService redisClient;
	//private GlobalQueue globalInbound;
	//private GlobalQueue globalOutbound;

	//private QueueManager queues;

	public GlobalHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
		this.redisClient= new RedisDBServiceImpl();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, GlobalMessage msg) throws Exception {
		logger.info("received global message");
		handleMessage(msg,ctx.channel());		
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
		
		else if(msg.hasRequest()){
			if(DBUtils.isfileExist(msg.getRequest().getFileName())){
				// we have the file
				// process it and put the global message into queue
				
				
				Map<Integer, byte[]> keyMap = redisClient.get(msg.getRequest().getFileName());
				
				if(!keyMap.isEmpty()){ 
			
					logger.info("TaskHandlerHelper: handleDBOperations() GetMap total size"+ keyMap.size());
					
					for(Map.Entry<Integer, byte[]> entry: keyMap.entrySet()){
						
						GlobalHeader.Builder gh = GlobalHeader.newBuilder();
						gh.setClusterId(state.getGlobalConf().getClusterId());
						gh.setTime(System.currentTimeMillis());
						//gh.setDestination();
						
						File.Builder fb = File.newBuilder();
						fb.setFilename(msg.getRequest().getFileName());
						fb.setChunkId(entry.getKey());
						fb.setTotalNoOfChunks(keyMap.size());
						fb.setData(ByteString.copyFrom(entry.getValue()));
						
						Response.Builder res = Response.newBuilder();
						res.setRequestType(msg.getRequest().getRequestType());
						res.setFile(fb);
						res.setRequestId(msg.getRequest().getRequestId());
						res.setSuccess(true);
						
						
					
						GlobalMessage.Builder responseMsg = GlobalMessage.newBuilder();
						responseMsg.setGlobalHeader(gh);
						responseMsg.setResponse(res);
						
						state.getGlobalOutboundQueue().put(responseMsg.build());
						
					
					}
				}
				
			}
			else{
				state.getgMon().pushMessagesIntoCluster(msg);
			}
		}
	}

	
	
}
