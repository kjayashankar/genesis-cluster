package com.genesis.router.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "conf")
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalConf {
	
	private int globalPort;

	private String globalHost;
	
	private List<RoutingEntry> routing;
	
	private int clusterId;
	
	
	public HashMap<String, Integer> asHashMap() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		if (routing != null) {
			for (RoutingEntry entry : routing) {
				map.put(entry.host, entry.port);
			}
		}
		return map;
	}

	public void addEntry(RoutingEntry entry) {
		if (entry == null)
			return;

		if (routing == null)
			routing = new ArrayList<RoutingEntry>();

		routing.add(entry);
	}
	
	public String getGlobalHost ()
	{
	    return globalHost;
	}
	
	public void setGlobalHost (String globalHost)
	{
	    this.globalHost = globalHost;
	}
	
	public List<RoutingEntry> getRouting ()
	{
	    return routing;
	}
	
	public void setRouting (List<RoutingEntry> routing)
	{
	    this.routing = routing;
	}
	

public int getGlobalPort() {
		return globalPort;
	}

	public void setGlobalPort(int globalPort) {
		this.globalPort = globalPort;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}


@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.PROPERTY)
public static final class RoutingEntry {
	private String host;
	private int port;
	private int clusterId;

	public RoutingEntry() {
	}

	public RoutingEntry(int clusterId, String host, int port) {
		this.clusterId = clusterId;
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

}

}
