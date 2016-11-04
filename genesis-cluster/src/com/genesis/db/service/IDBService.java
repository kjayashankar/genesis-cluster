package com.genesis.db.service;

import java.util.*;

public interface IDBService {

	String put(String key, int chunkID, byte[] value);
	
	/**
	 * create the mapping keys and return in response message
	 * @param value
	 * @return
	 */
	String store(byte[] value);
	
	public Map<Integer, byte[]> get(String key);
	
	public boolean update(String key, int chunkID, byte[] value);
	
	public Map<Integer, byte[]> delete(String key);
	
	public boolean containsKey(String key);
	
	public List<Integer> getChunkIDs(String key);
	
	public String getDatabaseType();
}
