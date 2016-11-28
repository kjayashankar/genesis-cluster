package com.genesis.helper;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.db.handlers.RedisDBServiceImpl;
import com.genesis.db.service.IDBService;
import com.genesis.queues.Queue;
import com.genesis.resource.ResourceUtil;
import com.genesis.router.container.RoutingConf;
import com.genesis.router.server.ServerState;
import com.message.ClientMessage.RequestMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import pipe.common.Common.Failure;
import pipe.work.Work.WorkMessage;
import routing.Pipe.CommandMessage;
import com.genesis.db.handlers.MongoDBServiceImpl;

public class TaskHandlerHelper {

	private static Logger logger = LoggerFactory.getLogger("TaskHandlerHelper");
	private ServerState state;
	//TODO Write to pick it from the Factory, fine for now
	IDBService redisClient ;
	static IDBService mongoDBServiceImpl;
	
	
	public static IDBService getMongoConnection(){
		
		try{
			logger.info("Got mongo Object");
			mongoDBServiceImpl = new MongoDBServiceImpl();
		}
		catch(Exception e){}
		return mongoDBServiceImpl;
	}
	
	Queue outboundQueue;
	private ConcurrentHashMap<String, SocketAddress> keySocketMappings;
	private ConcurrentHashMap<SocketAddress, Channel> addressChannelMappings;

	public TaskHandlerHelper(ServerState state) {
		if (state != null) {
			this.state = state;
			try{
			this.outboundQueue = state.getQueueMonitor().getOutboundQueue();
			}catch(Exception e){
				e.printStackTrace();
			}
			this.redisClient= new RedisDBServiceImpl();
			
		}
	}
	
	
	public void handleMessage(CommandMessage msg, Channel channel) {
		if (msg == null) {
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}
		
		keySocketMappings = state.getKeySocketMappings();
		addressChannelMappings = state.getAddressChannelMappings();
		
		if(keySocketMappings.containsKey(msg.getReqMsg().getKey())){
			keySocketMappings.put(msg.getReqMsg().getKey(), channel.remoteAddress());
			addressChannelMappings.put(channel.remoteAddress(), channel);
			channel.closeFuture().addListener(new CloseConnectionListener());
		}
		
		/*if (!keySocketMappings.containsKey(msg.getReqMsg().getKey())) {
			keySocketMappings.put(msg.getReqMsg().getKey(), channel.remoteAddress());
			addressChannelMappings.put(channel.remoteAddress(), channel);
			channel.closeFuture().addListener(new CloseConnectionListener());
		}*/
		try {
			handleDBOperations(msg, channel);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void doMessageForwardingToClient(CommandMessage msg, Channel channel){

		try {
			
			logger.info("sending to client updated messages, msg.hasReq - " + msg.hasReqMsg() + ",has msg.getChunkInfo ??? " +msg.getReqMsg().hasChunkInfo());
			
			WorkMessage returnWork = ResourceUtil.buildWorkMessageFromCommandMsg(msg, state);
			
			outboundQueue.put(returnWork, channel);
			
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
	}
	
	private CommandMessage discardDuplicate(CommandMessage msg) {

		// Message is for current node.

		if (msg.getHeader().getDestination() == state.getConf().getNodeId()) {
			logger.info("Same message received by source! Dropping message...");
			return null;
		}
		//return route0(msg);
		return msg;
	}
	/**
	 * Handle db storage and retrival 
	 */
	
	private void handleDBOperations(CommandMessage msg, Channel channel){
		try {
			RequestMessage requestMessage = msg.getReqMsg();
			
			
			if(requestMessage!=null){
				switch (msg.getReqMsg().getOperation()){
					
				case GET: 
					
					logger.info("----- Getting key from DataBase ----" + requestMessage.getKey());
					Map<Integer, byte[]> keyMap = redisClient.get(requestMessage.getKey());
					
					if(keyMap.isEmpty()){
						CommandMessage failureMsg = ResourceUtil.createResponseFailureMessage(msg, state);
						logger.info("Sending failure message");
						 doMessageForwardingToClient(failureMsg, channel);
					} else {
						
						logger.info("TaskHandlerHelper: handleDBOperations(): GET giving message back to the client-- total count of messages "+ keyMap.size());
						
						for(Map.Entry<Integer, byte[]> entry: keyMap.entrySet()){
							
							CommandMessage returnMsg = ResourceUtil.createResponseCommandMessage(msg, entry.getValue(), entry.getKey(), state, keyMap.size());
							doMessageForwardingToClient(returnMsg, channel);
							
						}
					}
					
					
					break;
					
				case PUT: 
					//To release excessive memory taken up by the file.
					if(redisClient.noOfChunksExisting(requestMessage.getKey()) > requestMessage.getNoOfChunks()){
						redisClient.deleteExcess(requestMessage.getKey(), requestMessage.getNoOfChunks());
					}
					
					logger.info("Key is inside put "+requestMessage.getKey());
					logger.info("----- Updating key into DataBase ----");
					boolean updateKey = redisClient.put(requestMessage.getKey(), requestMessage.getSeqNo(), requestMessage.getData().toByteArray());
					
					logger.info("---- PUT: key stored" +updateKey+ " for Sequence ----"+ requestMessage.getSeqNo());
					
					
					/*boolean updateKeyMongo = mongoDBServiceImpl.put(requestMessage.getKey(), requestMessage.getSeqNo(), requestMessage.getData().toByteArray());
					logger.info("---- Key received  ----"+ updateKeyMongo);*/
					
					break;
				case POST:
					
					logger.info("----- Storing key into DataBase ----");
					String keyStored = redisClient.post(requestMessage.getKey(), requestMessage.getSeqNo(), requestMessage.getData().toByteArray());
					
					logger.info("---- POST: key stored" +keyStored+ " for Sequence ----"+ requestMessage.getSeqNo());
				
					/*String keyStoredMongo = mongoDBServiceImpl.post(requestMessage.getKey(), requestMessage.getSeqNo(), requestMessage.getData().toByteArray());
					logger.info("---- Key stored ----"+ keyStoredMongo);*/
					break;
				case DEL:
					
					logger.info("----- deleting key from DataBase ----");
					boolean deletedKey = redisClient.delete(requestMessage.getKey());
					logger.info("---- Key deleted ----"+ deletedKey);
					
					/*boolean deletedKeyMongo = mongoDBServiceImpl.delete(requestMessage.getKey());
					logger.info("---- Key deleted ----"+ deletedKeyMongo);*/
					
					break;
					
				case STEAL: 
					//do nothing
					break;
					
				default: 
					logger.info("---- No matching operation was found ----");
				}
			}

		} catch (Exception e) {
			// TODO add logging
			logger.info("Exception in operation , "+ msg.getReqMsg().getOperation() + ", " + e.getMessage());
			e.printStackTrace();
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			CommandMessage.Builder rb = CommandMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}
	}
	
	public class CloseConnectionListener implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			logger.info("Connection broke with "+future.channel().remoteAddress());
			logger.info(keySocketMappings.toString());
			logger.info(addressChannelMappings.toString());

			addressChannelMappings.remove(future.channel().remoteAddress());
			String tempKey  = null;
			for(Map.Entry<String, SocketAddress> entry : keySocketMappings.entrySet()){
				if(future.channel().remoteAddress() == entry.getValue()){
					tempKey = entry.getKey();
				}
			}
			// To remove the key from both mapping if connection is not there
			if(tempKey!=null && keySocketMappings.containsKey(tempKey)){
				keySocketMappings.remove(tempKey);
			}
			
		}
	}
	
	
}
