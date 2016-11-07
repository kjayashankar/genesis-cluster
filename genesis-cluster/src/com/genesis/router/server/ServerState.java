package com.genesis.router.server;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.genesis.monitors.NetworkMonitor;
import com.genesis.monitors.QueueMonitor;
import com.genesis.router.container.RoutingConf;
import com.genesis.router.server.edges.EdgeMonitor;

import io.netty.channel.Channel;


public class ServerState {
	private RoutingConf conf;
	private EdgeMonitor emon;
	private NetworkMonitor networkmon;
	private QueueMonitor queueMonitor;
	private ConcurrentHashMap<String, SocketAddress> keySocketMappings = new ConcurrentHashMap<>();
	private ConcurrentHashMap<SocketAddress, Channel> addressChannelMappings = new ConcurrentHashMap<>();
	
	public STATE state = STATE.ORPHAN; 
	
	public ConcurrentHashMap<String, SocketAddress> getKeySocketMappings(){
		
		if(keySocketMappings!=null){
			return keySocketMappings;
		}else{
			return new ConcurrentHashMap<>();
		}
		
	}
	
	public void setQueueMonitor(QueueMonitor qMon){
		this.queueMonitor = qMon;
	}
	
	public QueueMonitor getQueueMonitor(){
		return queueMonitor;
	}
	

	public NetworkMonitor getNetworkmon() {
		return networkmon;
	}

	public void setNetworkmon(NetworkMonitor networkmon) {
		this.networkmon = networkmon;
	}

	public RoutingConf getConf() {
		return conf;
	}

	public void setConf(RoutingConf conf) {
		this.conf = conf;
	}

	public EdgeMonitor getEmon() {
		return emon;
	}

	public void setEmon(EdgeMonitor emon) {
		this.emon = emon;
	}

	public ConcurrentHashMap<SocketAddress, Channel> getAddressChannelMappings() {
		if(addressChannelMappings!=null){
			return addressChannelMappings;
		}else{
			return new ConcurrentHashMap<>();
		}
		
	}

}
