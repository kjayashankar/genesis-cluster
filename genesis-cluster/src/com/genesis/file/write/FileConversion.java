
package com.genesis.file.write;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.ByteString;

/**
 * @author navdeepdahiya
 *
 */
public class FileConversion {

	private static int chunkSize = 1024*1024; //1 Mb kept for now.
	
	public static int getBytesAvailable(BufferedInputStream bcs){
		int bytesNo =0;
		//boolean flag = false;
		
		try {
			bytesNo = bcs.available();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		} 

		return bytesNo;
	}
	
	public static int noOfChunksToCreate(BufferedInputStream bufferedInputStream){
		//System.out.println("getBytesAvailable(bufferedInputStream) "+ getBytesAvailable(bufferedInputStream)
		//+ ", chunkSize "+ chunkSize +" no of Chunks will be created " + ((getBytesAvailable(bufferedInputStream)/chunkSize) +1));
		
		
		return (int) ((getBytesAvailable(bufferedInputStream)/chunkSize) +1);
	}
	
	public static List<ByteString> readAndConvert(String file) {

		List<ByteString> output = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			
			byte[] data = new byte[chunkSize];
			bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			output = new LinkedList<ByteString>();
			int chunksNo = noOfChunksToCreate(bufferedInputStream);
			
			System.out.println();
			//TODO Check till what point loop should go
			for(int i=0; i<chunksNo ; i++){
				
				//bufferedInputStream.skip(i * chunkSize);		
				if(bufferedInputStream.read(data) > 0){
					/*if(( i== chunksNo -1 ) && (bufferedInputStream.available() < chunkSize)){
						output.add(ByteString.copyFrom(data));
					}
					else{
						output.add(ByteString.copyFrom(data));
					}*/
					//bufferedInputStream.skip(i * chunkSize);
					//System.out.println("Added sequence"+ i);
					output.add(ByteString.copyFrom(data));
				}
				
			}
			
			//bufferedInputStream.skip(from * chunkSize);
			//int index = 0;
			
			// Read file
			/*while (bufferedInputStream.read(data) > 0 && (N == -1 || index++ < N)) {
				output.add(ByteString.copyFrom(data));
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return output;
	}

	/**
	 * file - place to store file. 
	 * chunkList : list of chunks recieved from DB
	 * 
	 */
	
	public static void convertAndWrite(String file, List<ByteString> chunkList) {
		BufferedOutputStream buffOutStream = null;
		try {
			buffOutStream = new BufferedOutputStream(new FileOutputStream(file));
			for (ByteString data : chunkList) {
				buffOutStream.write(data.toByteArray());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				buffOutStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
