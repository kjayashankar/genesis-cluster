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
import global.Global.RequestType;
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
	protected void channelRead0(ChannelHandlerContext ctx, GlobalMessage msg)  {
		try{
			logger.info("received global message "+msg);
			handleMessage(msg,ctx.channel());		
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected void handleMessage(GlobalMessage msg,Channel channel){
		logger.info("GH: handleMessage() is :: "+ msg.hasRequest());
		if(msg.hasPing()){
			logger.info("GH: hasPing() is :: "+ msg.hasPing());
			state.getgMon().sendPing(msg);
		}
		else if(msg.hasResponse()){
			
			logger.info("GH: Has got response :: "+ msg.hasResponse() + ", Response msg from cluster"+ msg);
			if(state.getGlobalConf().getClusterId() == msg.getGlobalHeader().getDestinationId()){
				// my cluster request;
				String uuid = msg.getResponse().getRequestId();
				// check if i have the channel
				if(state.moderator.containsKey(uuid)){
					Channel chn = state.moderator.get(uuid);
				//convert into CommandRespnse Message
					if(chn != null && chn.isActive() && chn.isWritable() ){
						logger.info("pushing into client");
						CommandMessage response =
						ResourceUtil.convertIntoCommand(msg);
						chn.writeAndFlush(response);
					}
					else {
						logger.info("channel is not writable");
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
			
			// Don't do anything, it is done.
			if(msg.getGlobalHeader().getDestinationId() == state.getGlobalConf().getClusterId())
				return;
			//TODO add for all the cases! How about WRITE/UPDATE operation? Key will not be there
			
			logger.info("Data available is :: "+ msg.hasRequest());
			
			
			switch (msg.getRequest().getRequestType()){
			
			case READ:
				if(DBUtils.isfileExist(msg.getRequest().getFileName())){

					logger.info("Global read for file ==> "+ msg.getRequest().getFileName());
					Map<Integer, byte[]> keyMap = redisClient.get(msg.getRequest().getFileName());	
					if(!keyMap.isEmpty()){
						logger.info("GH: handleGlobalMesage: GET giving message back to the client\n\t\t-- total count of messages "+ keyMap.size());
						for(Map.Entry<Integer, byte[]> entry: keyMap.entrySet()){
							GlobalMessage globalReturnMsg = ResourceUtil.createGlobalResponseMessage(msg, entry.getValue(), entry.getKey(), state, keyMap.size());
							channel.writeAndFlush(globalReturnMsg);
						}
					}
				}
				else{
					state.getgMon().pushMessagesIntoCluster(msg);
				}

				break;
			
			case WRITE:
				//This will not write if the file is already there
				redisClient.post(msg.getRequest().getFileName(), msg.getRequest().getFile().getChunkId(), msg.getRequest().getFile().getData().toByteArray());
				logger.info("Global WRITE for file ==> "+ msg.getRequest().getFileName() +", chunk No. "+ msg.getRequest().getFile().getChunkId());
				state.getgMon().pushMessagesIntoCluster(msg);

				break;
			
			case UPDATE:
				boolean keyUpdated = redisClient.put(msg.getRequest().getFileName(), msg.getRequest().getFile().getChunkId(), msg.getRequest().getFile().getData().toByteArray());
				logger.info("Global UPDATE for file is ==> "+ keyUpdated +", chunk No. "+ msg.getRequest().getFile().getChunkId());
				state.getgMon().pushMessagesIntoCluster(msg);

				break;
			
			case DELETE: 
				logger.info("Global read for file ==> "+ msg.getRequest().getFileName());
				boolean deletedKey = redisClient.delete(msg.getRequest().getFileName());
				logger.info("GH: handleGlobalMessage: DELETE key globally!!! "+ deletedKey);
				state.getgMon().pushMessagesIntoCluster(msg);

				break;
				
			default:
				logger.info("GH: No matching cases!");
				break;
				
			}
			
		}
	}
	
	

	private void globalResponseForwarding(GlobalMessage returnMsg, Channel channel) {
		
		
	}

	
	
}
