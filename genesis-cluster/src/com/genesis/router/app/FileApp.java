package com.genesis.router.app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.file.write.FileConversion;
import com.genesis.router.client.CommConnection;
import com.genesis.router.client.CommListener;
import com.genesis.router.client.MessageClient;
import com.google.protobuf.ByteString;
import com.message.ClientMessage.Operation;
import com.message.ClientMessage.ResponseMessage;

import routing.Pipe.CommandMessage;

public class FileApp implements CommListener {
	protected static Logger logger = LoggerFactory.getLogger("DemoApp");
	private MessageClient mc;
	private int noOfChunks = -1; 
	private List<ResponseMessage> responseList;
	private Map<String, Integer> keyChunkNoMap;
	String filePath;
	String outputFilePath;
	
	public FileApp(MessageClient mc) {
		init(mc);
		this.responseList = new LinkedList<>();
		keyChunkNoMap = new HashMap<String,Integer>();
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
			//mc.delete("First_One"); got incoming message
			
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

		//TODO write check for failuremsg

		
		int chunkData = 0;
		
		if (msg.getResMsg().getOperation() == Operation.GET) {
			
			//noOfChunks = keyChunkNoMap.get(msg.getResMsg().getKey());
			
			logger.info("Beginning to read message... ");
			logger.info("It has no of chunks...");
			if (msg.getResMsg().hasChunkInfo()) {
				chunkData = msg.getResMsg().getChunkNo();
				logger.info("Got File from the server, reassembling chunks, no of chunks are : "+ chunkData);
				//TODO Do I need this data? come back later.
				
				
			} else {
				ByteString data = msg.getResMsg().getData();

				responseList.add(msg.getResMsg());

				logger.info("   ---   "+ responseList.size() + ", noOfChunks " + noOfChunks);
				if (responseList.size() == noOfChunks) {
				logger.info("Complete response is now received."); 	
				
					Collections.sort(responseList, new Comparator<ResponseMessage>() {
						@Override
						public int compare(ResponseMessage resp1, ResponseMessage resp2) {
							//TODO check if this is working fine
							Integer chunkRes1 = resp1.getChunkNo();
					    	Integer chunkRes2 = resp2.getChunkNo();
					    	
					    	int comp = chunkRes1.compareTo(chunkRes2);
					    		
					    	
					    	return (comp != 0 ? comp : chunkRes1.compareTo(chunkRes1));
					    	
					    	/*
					    	if(comp>0)
					    		return -1;
					        else if(comp<0)
					        	return 1;
					        else
					            return 0;*/
					    	
						}
					});

					
					List<ByteString> finalResList = new LinkedList<ByteString>();
					for (ResponseMessage response : responseList) {
						System.out.println("Sequencing m printing "+response.getChunkNo());
						finalResList.add(response.getData());
					}
					
						//FileConversion convertUtil = new FileConversion();
						FileConversion.convertAndWrite(outputFilePath, finalResList);
						logger.info("File written...");
					
					
					
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
		
		int port = 4168;
		
		
		
		if (args.length == 0) {
			System.out.println("usage: server <config file>");
			System.exit(1);
		}

		File cf = new File(args[0]);
		
		
		try {
			MessageClient mc = new MessageClient(host, port,cf);
			
			FileApp fa = new FileApp(mc);
			fa.clientFileOperation(args[1]);
			
			System.out.println("\n** exiting in 10 seconds. **");
			System.out.flush();
			Thread.sleep(1000 * 2000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommConnection.getInstance().release();
		}
	}
	
	public void clientFileOperation(String params) throws FileNotFoundException {
		
		String paramArr[] = params.split("\\|");
		//System.out.println("Array length is \n"+ paramArr.length 
			//	+ " Array string ,"+ params.toString());
		
		/** 
		 * Need to set below as command line arguments
		 * Java_Programming_with_BlueJ.pdf|POST|my_book
		 * Java_Programming_with_BlueJ.pdf|GET|my_book
		 * Filename|GET|key
		 * Need operations below to perform the operation on server
		 * 1) Input file Path
		 * 2) Operation
		 * 3) Key to work with 
		 */
		
		if(paramArr.length < 3){
			logger.info("Insufficient input parameters. Expected format is :: Filename|operation|key");
			return; 
		}
		
		if (paramArr[0]  == null || "".equals(paramArr[0])) {
			logger.info("No File Name was found...");
			return;
		}
		
		if (paramArr[1]  == null || "".equals(paramArr[1])) {
			logger.info("No operation was found...");
			return;
		}
		
		if (paramArr[2]  == null || "".equals(paramArr[2])) {
			logger.info("No Key was found, Will be generated and returned in the response." );
			return;
		}

		String key = paramArr[2];
		String fileName = paramArr[0];
		//FileConversion fileUtil = new FileConversion(); No need made it static utility
		
		filePath = "src/com/genesis/file/write/"+fileName;
		outputFilePath = "src/com/genesis/file/write/output/"+fileName;
		
		File tempFile = new File(filePath);
		long fileSize = tempFile.length();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath));
		int noOfChunks = FileConversion.noOfChunksToCreate(bufferedInputStream);
		keyChunkNoMap.put(key, noOfChunks);
		
		switch (paramArr[1]) {
		case "GET":
					logger.info("performing get from client");
					mc.get(key);
					break;

		case "POST":
					logger.info("Performing post from client");
					if (!tempFile.exists()) {
						throw new FileNotFoundException(filePath);
					}
					
					logger.info("Post Messages chunkSize is "+ noOfChunks + ", filePath "+ filePath);
					mc.postChunkInfo(key, noOfChunks, fileSize);
					//for (int i = 0; i < noOfChunks; i++) {
						List<ByteString> dataList = FileConversion.readAndConvert(filePath);
		
						int seqNo = 1;
						
						for (ByteString data : dataList) {
							mc.post(key, seqNo++, data);
						}
						
						logger.info("total no. to be retreived ... "+ seqNo);
					//}
					break;

		case "PUT":
			
			
			if (!tempFile.exists()) {
				throw new FileNotFoundException(filePath);
			}
			
			mc.postChunkInfo(filePath, 0, fileSize);
			
			for (int i = 0; i < noOfChunks; i++) {
				dataList = FileConversion.readAndConvert(filePath);

				int sequenceNo = 1;
				for (ByteString data : dataList) {
					mc.post(key, sequenceNo++, data);
				}
			}
			break;


		case "DELETE":
			mc.delete(key);
			
			break;

		default:
			logger.info("Default invoked operation not correct. Please chk again! ");
			
			break;
		}

		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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