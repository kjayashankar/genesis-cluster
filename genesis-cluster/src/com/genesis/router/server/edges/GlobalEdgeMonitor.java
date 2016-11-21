package com.genesis.router.server.edges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.helper.GlobalInit;
import com.genesis.router.server.ServerState;

import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GlobalEdgeMonitor {

	private static Logger logger = LoggerFactory.getLogger("global edge monitor");
	private ServerState state;
	private EdgeList globalOutboud = new EdgeList();	
	
	public GlobalEdgeMonitor(ServerState state) {
		this.state = state;
	}
	
	public void pushMessagesIntoCluster(GlobalMessage global){
		for(EdgeInfo ei: globalOutboud.map.values()) {
			if(ei.getChannel() != null && ei.isActive()){
				ei.getChannel().writeAndFlush(global);
				break;
			}
			else{
				try{
					logger.info("trying to connect to cluster node " + ei.getRef());
					EventLoopGroup group = new NioEventLoopGroup();
					GlobalInit si = new GlobalInit(state, false);
					Bootstrap b = new Bootstrap();
					b.group(group).channel(NioSocketChannel.class).handler(si);
					b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);
					
					ChannelFuture channel = b.connect(ei.getHost(), ei.getPort()).syncUninterruptibly();
					
					ei.setChannel(channel.channel());
					ei.setActive(channel.channel().isActive());
				}
				catch(Exception e){
					logger.error("Failed connecting to other cluster node "+ei.getRef()+" , i'm clueless");
				}
			}
		}
	}
	
}
