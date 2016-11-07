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
package com.genesis.router.app;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.router.client.CommConnection;
import com.genesis.router.client.CommListener;
import com.genesis.router.client.MessageClient;
import com.google.protobuf.ByteString;
import com.message.ClientMessage.Operation;
import com.message.ClientMessage.ResponseMessage;

import routing.Pipe.CommandMessage;

public class DemoApp implements CommListener {
	protected static Logger logger = LoggerFactory.getLogger("demo");
	private MessageClient mc;
	private long noOfChunks; 
	private List<ResponseMessage> responseList;

	public DemoApp(MessageClient mc) {
		init(mc);
	}

	
	
	private void init(MessageClient mc) {
		this.mc = mc;
		this.mc.addListener(this);
	}

	private void ping(int N) {
		// test round-trip overhead (note overhead for initial connection)
		final int maxN = 10;
		long[] dt = new long[N];
		long st = System.currentTimeMillis(), ft = 0;
		for (int n = 0; n < N; n++) {
			mc.ping();
			ft = System.currentTimeMillis();
			dt[n] = ft - st;
			st = ft;
		}

		System.out.println("Round-trip ping times (msec)");
		for (int n = 0; n < N; n++)
			System.out.print(dt[n] + " ");
		System.out.println("");
	}

	private void post(int N) {
		final int maxN = 10;
		long[] dt = new long[N];
		long st = System.currentTimeMillis(), ft = 0;
		for (int n = 0; n < N; n++) {
			//mc.clientRequest();
			mc.post("First_One", 1, ByteString.copyFromUtf8("For the First chunk info is stored"));
			//mc.post("First_One", 2, ByteString.copyFromUtf8("For the Second chunk info is stored"));
			//mc.get("First_One");
			//mc.put("First_One", 2, ByteString.copyFromUtf8("For the Second chunk info is thirdly a bit"));
			mc.get("First_One");
			//mc.delete("First_One");
			
			ft = System.currentTimeMillis();
			dt[n] = ft - st;
			st = ft;
		}

		//System.out.println("Post to my database (msec)"+);
		for (int n = 0; n < N; n++)
			System.out.print(dt[n] + " ");
		System.out.println("");
	}
	
	
	
	@Override
	public String getListenerID() {
		return "demo";
	}

	@Override
	public void onMessage(CommandMessage msg) {

		

		
		
		if (msg.getResMsg().getOperation() == Operation.GET) {

			logger.info("Got File from the server, reassembling chunks, no of chunks are : "+ msg.getResMsg().getChunkInfo().getSeqSize());
			
			if (msg.getResMsg().hasChunkInfo()) {
				noOfChunks = msg.getResMsg().getChunkInfo().getSeqSize();
			} else {
				ByteString data = msg.getResMsg().getData();

				
				responseList.add(msg.getResMsg());

				
				if (responseList.size() == noOfChunks) {
				logger.info("Complete response is now received."); 	
					
					
					
					Collections.sort(responseList, new Comparator<ResponseMessage>() {
						@Override
						public int compare(ResponseMessage resp1, ResponseMessage resp2) {
							
							Integer chunkRes1 = resp1.getChunkNo();
					    	Integer chunkRes2 = resp2.getChunkNo();
					    	
					    	
					    	int comp = chunkRes1.compareTo(chunkRes2);

					    		if(comp>0)
					    			return -1;
					            else if(comp<0)
					                return 1; 
					            else 
					                return 0;
					    	
						}
					});

					List<ByteString> finalResList = new LinkedList<ByteString>();
					for (ResponseMessage response : responseList) {
						finalResList.add(response.getData());
					}

					
					/*
						FileConversion util = new FileConversion();
						util.convertAndWrite(filepath, list);*/
					
					
					
				}
			}
		}
	}

	/**
	 * sample application (client) use of our messaging service client lost connection to the server
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "127.0.0.1";
		//int port = 4568;

		int port = 4168;
		
		if (args.length == 0) {
			System.out.println("usage: server <config file>");
			System.exit(1);
		}

		File cf = new File(args[0]);
		
		
		try {
			MessageClient mc = new MessageClient(host, port,cf);
			//mc.init(cf);
			DemoApp da = new DemoApp(mc);

			// do stuff w/ the connection
			//da.ping(2);
			da.post(1);

			
			System.out.println("\n** exiting in 10 seconds. **");
			System.out.flush();
			Thread.sleep(50 * 2000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommConnection.getInstance().release();
		}
	}
}
/*class ResponseComparator implements Comparator<ResponseMessage> {
    public int compare(Object o1, Object o2) {
    	ResponseMessage resp1 = (ResponseMessage)o1;
    	ResponseMessage resp2 = (ResponseMessage)o2;

    	Integer chunkRes1 = resp1.getChunkNo();
    	Integer chunkRes2 = resp2.getChunkNo();
    	
    	
    	int comp = chunkRes1.compareTo(chunkRes2);

    		if(comp>0)
    			return -1;
            else if(comp<0)
                return 1; 
            else 
                return 0;
    	
       
    	
    }

	@Override
	public int compare(ResponseMessage resp1, ResponseMessage resp2) {
		
		Integer chunkRes1 = resp1.getChunkNo();
    	Integer chunkRes2 = resp2.getChunkNo();
    	
    	
    	int comp = chunkRes1.compareTo(chunkRes2);

    		if(comp>0)
    			return -1;
            else if(comp<0)
                return 1; 
            else 
                return 0;
    	
	}
}*/