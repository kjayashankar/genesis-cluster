package com.genesis.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.db.handlers.DBUtils;
import com.genesis.db.handlers.RedisDBServiceImpl;
import com.genesis.db.service.IDBService;
import com.genesis.resource.ResourceUtil;
import com.genesis.router.server.STATE;
import com.genesis.router.server.ServerState;
import com.google.protobuf.ByteString;
import com.message.ClientMessage.RequestMessage;
import com.message.ClientMessage.ResponseMessage;

import global.Global.File;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import global.Global.RequestType;
import global.Global.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Header;
import routing.Pipe.CommandMessage;

public class GlobalHandler extends SimpleChannelInboundHandler<GlobalMessage> {

	protected static Logger logger = LoggerFactory.getLogger("Global Handler");

	protected ServerState state;
	IDBService redisClient;

	public GlobalHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
		this.redisClient= new RedisDBServiceImpl();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, GlobalMessage msg)  {
		//if(state.state == STATE.LEADER) {
			try{
				logger.info("received global message "+msg);
				handleMessage(msg,ctx.channel());		
			}
			catch(Exception e){
				e.printStackTrace();
			}
		//}
	}
	
	protected void handleMessage(GlobalMessage msg,Channel channel){
		if(msg.hasPing()){
			logger.info("received ping");
			state.getgMon().sendPing(msg);
		}
		else if(msg.hasResponse()){
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
				}
				// else give it to ring
			}
			else{
				//may be in other cluster?
				state.getgMon().pushMessagesIntoCluster(msg);
			}
		}
		
		else if(msg.hasRequest()){
			if(state.moderator.containsKey(msg.getRequest().getRequestId())){
				// return file is not available with anyone in cluster
				return ;
			}
			if(DBUtils.isfileExist(msg.getRequest().getFileName())){
				// we have the file
				// process it and put the global message into queue
				//logger.info("message is ");
				switch(msg.getRequest().getRequestType()) {
					case READ:{
						handleRead(msg,channel);
						break;
					}
					case UPDATE:{
						handleUpdate(msg,channel);
						break;
					}
					case DELETE:{
						handleDelete(msg,channel);
						break;
					}
				}
				
				state.getgMon().pushMessagesIntoCluster(msg);
				
			}
			else{
				state.getgMon().pushMessagesIntoCluster(msg);
			}
		}
		
	}

	private void handleDelete(GlobalMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	private void handleUpdate(GlobalMessage msg, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	private void handleRead(GlobalMessage msg, Channel channel) {
		Map<Integer, byte[]> keyMap = redisClient.get(msg.getRequest().getFileName());
		
		if(!keyMap.isEmpty()){ 
	
			logger.info("TaskHandlerHelper: handleDBOperations() GetMap total size"+ keyMap.size());
			
			for(Map.Entry<Integer, byte[]> entry: keyMap.entrySet()){
				
				GlobalHeader.Builder gh = GlobalHeader.newBuilder();
				gh.setClusterId(msg.getGlobalHeader().getClusterId());
				gh.setTime(System.currentTimeMillis());
				gh.setDestinationId(msg.getGlobalHeader().getDestinationId());
				
				File.Builder fb = File.newBuilder();
				fb.setFilename(msg.getRequest().getFileName());
				fb.setChunkId(entry.getKey());
				fb.setTotalNoOfChunks(keyMap.size());
				fb.setData(ByteString.copyFrom(entry.getValue()));
				
				Response.Builder res = Response.newBuilder();
				res.setRequestType(msg.getRequest().getRequestType());
				res.setRequestId(msg.getRequest().getRequestId());
				res.setSuccess(true);
				
				GlobalMessage.Builder responseMsg = GlobalMessage.newBuilder();
				responseMsg.setGlobalHeader(gh);
				res.setFile(fb);
				responseMsg.setResponse(res);
				channel.writeAndFlush(responseMsg.build());
				//state.getGlobalOutboundQueue().put(responseMsg.build());
				
			
			}
		}
		
	}

	
	
}
