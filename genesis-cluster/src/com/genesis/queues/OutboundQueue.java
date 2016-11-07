package com.genesis.queues;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.server.ServerState;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import pipe.work.Work.WorkMessage;

public class OutboundQueue implements Queue{

	private static Logger logger = LoggerFactory.getLogger("outboud queue");
	private LinkedBlockingDeque<WorkChannel> outbound;
	private ServerState state; 
	private ConcurrentHashMap<String, SocketAddress> keySocketMappings;
	private ConcurrentHashMap<SocketAddress, Channel> addressChannelMappings;

	private int processed = 0;
	
	public OutboundQueue(ServerState state) {
		this.state = state;
		outbound = new LinkedBlockingDeque<WorkChannel>();
	}

	@Override
	public void put(WorkMessage work, Channel channel) {
		outbound.add(new WorkChannel(work, channel));
	}

	@Override
	public WorkChannel get() {
		try {
			return outbound.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSize() {
		return outbound.size();
	}
	
	
	public WorkMessage getWorkMessage(){
		WorkChannel tc= get();
		if(tc != null ){
			return tc.getWorkMessage();
		}
		return null;
	}
	
	public boolean process(){
		if(outbound.size() == 0) {
			logger.info("outbound queue size is 0, process other queues, may be lazy ?");
			return false;
		}
		WorkChannel t = get();
		WorkMessage work = t.getWorkMessage();
		Channel channel = t.getChannel();
		
		keySocketMappings = state.getKeySocketMappings();
		addressChannelMappings = state.getAddressChannelMappings();
		
		logger.info("addressChannelMappings values"+addressChannelMappings);
		logger.info("");
		
		
		logger.info("Writing response back to the client");
		logger.info("channel state : "+ channel.isActive() + ", Channel is open"+ channel.isOpen());
		if(channel.isActive() && channel.isOpen() && channel.isWritable()){
			logger.info("Message Key :: "+work.getTask().getCommandMessage());
			ChannelFuture future = channel.writeAndFlush(work.getTask().getCommandMessage());
			future.awaitUninterruptibly();
			System.out.println("Written to channel");
			boolean ret = future.isSuccess();
			if(!ret){
				logger.error("Error in sending message");
				logger.error("Reason : "+future.cause() );
				put(work,channel);
			}
		}
		

		
		
			/*if (keySocketMappings.containsKey(work.getTask().getCommandMessage().getResMsg().getKey())) {
				SocketAddress addr = keySocketMappings.get(work.getTask().getCommandMessage().getResMsg().getKey());
				
				if (addressChannelMappings.containsKey(addr)) {
					//if(discardDuplicate(work.getTask().getCommandMessage())!=null){
						//outboundQueue.put(returnWork, channel);
						//outboundQueue.put(workMessage, channel);
					//}
						
					channel.writeAndFlush(work.getTask().getCommandMessage());
				} else {
					
					keySocketMappings.remove(work.getTask().getCommandMessage().getResMsg().getKey());
				}
			} else {
				logger.info("No Client is waiting for the response....");
			}
		}*/
		processed++;
		return true;
	}
	
	@Override
	public WorkMessage rebalance() {
		logger.error("operation not supported in this type of queue");
		return null;
	}
	@Override
	public int numEnqueued() {
		// TODO Auto-generated method stub
		return outbound.size();
	}

	@Override
	public int numProcessed() {
		// TODO Auto-generated method stub
		return processed;
	}
}
