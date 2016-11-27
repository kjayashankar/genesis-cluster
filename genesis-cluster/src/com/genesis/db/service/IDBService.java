package com.genesis.db.service;

import java.util.*;

public interface IDBService {

	String post(String key, int chunkID, byte[] value);
	
	String post(byte[] value);
	
	public Map<Integer, byte[]> get(String key);
	
	public boolean put(String key, int chunkID, byte[] value);
	
	public boolean delete(String key);
	
	public boolean containsKey(String key);
	
	public List<Integer> getChunkIDs(String key);
	
	public String getDatabaseType();


	int noOfChunksExisting(String fileName);

	boolean deleteExcess(String key, int count);
}
