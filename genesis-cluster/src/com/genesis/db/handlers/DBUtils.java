package com.genesis.db.handlers;

import java.util.Map;

import com.genesis.db.service.IDBService;

public class DBUtils {

	public static boolean isfileExist(String fileName) {
		IDBService redisClient = new RedisDBServiceImpl() ;
		return redisClient.containsKey(fileName);
	}
}
