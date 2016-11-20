package com.genesis.router.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import pipe.common.Common.Header;
import pipe.work.Work.WorkMessage;

public class GlobalThread extends Thread {

	private boolean isinitialized = false;
	private Logger logger = LoggerFactory.getLogger("global thread");
	WorkMessage.Builder wm ;
	Channel channel = null;
	
	private ServerState state = null;
	
	public void setState(ServerState state){
		this.state = state;
	}
	
	public void setAddress(String host,int port){
		EventLoopGroup group = new NioEventLoopGroup();
		WorkInit si = new WorkInit(state, false);
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(si);
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		
		ChannelFuture channel = b.connect(host, port).syncUninterruptibly();
		
	}
	
	private void init(){
		
		wm = WorkMessage.newBuilder();
		Header.Builder header = Header.newBuilder();
		header.setNodeId(100);
		header.setTime(System.currentTimeMillis());
		wm.setPing(true);
		wm.setHeader(header);
	
		isinitialized = true;
	}
	
	
	public void run(){
		while(1==1){
			if(!isinitialized)
				init();
			if(channel != null){
				logger.info("pushing global beat======================");
				channel.writeAndFlush(wm);
			}
			else{
				logger.error("channel is not configured");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}
	
	
}
