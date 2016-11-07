/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.genesis.router.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.genesis.router.container.RoutingConf;
import com.genesis.router.server.MessageServer.JsonUtil;
import com.google.protobuf.ByteString;
import com.message.ClientMessage;
import com.message.ClientMessage.ChunkInfo;
import com.message.ClientMessage.Operation;
import com.message.ClientMessage.RequestMessage;

import pipe.common.Common;
import pipe.common.Common.Header;
import pipe.common.Common.Node;
import routing.Pipe.CommandMessage;



/**
 * front-end (proxy) to our service - functional-based
 * 
 * @author gash
 * 
 */
public class MessageClient {
	// track requests
	private long curID = 0;
	protected RoutingConf conf;
	private String host; 
	private int port;
	
	public void init(File cfg) {
		if (!cfg.exists())
			throw new RuntimeException(cfg.getAbsolutePath() + " not found");
		// resource initialization - how message are processed
		BufferedInputStream br = null;
		try {
			byte[] raw = new byte[(int) cfg.length()];
			br = new BufferedInputStream(new FileInputStream(cfg));
			br.read(raw);
			conf = JsonUtil.decode(new String(raw), RoutingConf.class);
			if (!verifyConf(conf))
				throw new RuntimeException("verification of configuration failed");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean verifyConf(RoutingConf conf) {
		return (conf != null);
	}
	
	public MessageClient(String host, int port, File confFile) {
		this.host= host;
		this.port = port;
		init(host, port);
		init(confFile);
		
	}
	
	

	private void init(String host, int port) {
		CommConnection.initConnection(host, port);
	}

	public void addListener(CommListener listener) {
		CommConnection.getInstance().addListener(listener);
	}


	public void ping() {
		// construct the message to send
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(7);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(2);

		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setPing(true);

		try {
			// direct no queue
			// CommConnection.getInstance().write(rb.build());

			// using queue
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void clientRequest() {
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(999);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);
		
		ChunkInfo.Builder cb =  ChunkInfo.newBuilder();
		cb.setNoOfChunks(1);
		
		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		reqMsg.setData(ByteString.copyFromUtf8("Add some random data"));
		reqMsg.setOperation(Operation.POST);
		reqMsg.setSeqNo(1);
		
		
		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setPing(true);
		
		rb.setReqMsg(reqMsg);
		
		try {
			
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void post(ByteString data) {
		Header.Builder hb = buildHeader();
		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		reqMsg.setData(data);
		reqMsg.setOperation(Operation.POST);
		//reqMsg.setChunkInfo(cb);
		reqMsg.setKey("First_One");
		reqMsg.setSeqNo(1);

		cb.setReqMsg(reqMsg);

		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void get(String key) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		//reqMsg.setData(ByteString.copyFromUtf8("Add some random data for post method."));
		reqMsg.setOperation(Operation.GET);
		//reqMsg.setChunkInfo(cb);
		reqMsg.setKey(key);
		//reqMsg.setSeqNo(1);

		cb.setReqMsg(reqMsg);

		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void delete(String key) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		reqMsg.setOperation(Operation.DELETE);
		reqMsg.setKey(key);

		cb.setReqMsg(reqMsg);

		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void post(String key, int sequenceNo, ByteString data) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		reqMsg.setData(data);
		reqMsg.setKey(key);
		
		reqMsg.setOperation(Operation.POST);
		
		reqMsg.setSeqNo(sequenceNo);
		
		cb.setReqMsg(reqMsg);
		
		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void put(String key, int sequenceNo, ByteString data) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		reqMsg.setData(data);
		reqMsg.setKey(key);
		
		reqMsg.setOperation(Operation.PUT);
		//reqMsg.setChunkInfo(cb);
		reqMsg.setSeqNo(sequenceNo);
		
		cb.setReqMsg(reqMsg);
		
		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void putChunksInfo(String key, int seqSize, long fileLength) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);
		
		RequestMessage.Builder reqMsg = RequestMessage.newBuilder();
		//reqMsg.setData(ByteString.copyFromUtf8("Add some random data for post method."));
		reqMsg.setKey(key);
		
		reqMsg.setOperation(Operation.POST);
		reqMsg.setSeqNo(0); 	//Always 0 for ChunkInfo

		ChunkInfo.Builder chb =  ChunkInfo.newBuilder();
		chb.setNoOfChunks(1);
		chb.setSeqSize(10);
		chb.setTime(System.currentTimeMillis());
		
		
		reqMsg.setChunkInfo(chb);
		cb.setReqMsg(reqMsg);

		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postChunkInfo(String key, int seqSize, long size) {
		Header.Builder hb = buildHeader();

		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setHeader(hb);

		RequestMessage.Builder qb = RequestMessage.newBuilder();
		qb.setOperation(Operation.POST);
		qb.setKey(key);
		qb.setSeqNo(seqSize);

		ChunkInfo.Builder mb = ChunkInfo.newBuilder();
		mb.setNoOfChunks(seqSize);
		mb.setSeqSize(size); 
		mb.setTime(System.currentTimeMillis());

		qb.setChunkInfo(mb);
		cb.setReqMsg(qb);

		try {
			CommConnection.getInstance().enqueue(cb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @return
	 */
	private Header.Builder buildHeader() {
		
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(conf.getNodeId());
		hb.setOrigin(buildNode());
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);
		return hb;
	}
	
	private Common.Node buildNode(){
		
			Node.Builder commNode = Node.newBuilder();
			
			commNode.setId(conf.getNodeId());
			commNode.setPort(port);
			commNode.setHost(host);
		
			return commNode.build();
	}


	public void release() {
		CommConnection.getInstance().release();
	}

	/**
	 * Since the service/server is asychronous we need a unique ID to associate
	 * our requests with the server's reply
	 * 
	 * @return
	 */
	private synchronized long nextId() {
		return ++curID;
	}
}
