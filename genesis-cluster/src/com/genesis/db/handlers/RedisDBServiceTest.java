package com.genesis.db.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis; 
import redis.clients.jedis.JedisPool; 
import redis.clients.jedis.JedisPoolConfig;

public class RedisDBServiceTest{
	
	public static void main(String[] args) {
		try {
			new RedisDBServiceTest().poolTest();
			new RedisDBServiceTest().singleTest();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Logger logger = LoggerFactory.getLogger("server");
/**
 * Poolテスト 
 * @throws Exception 
 */ 
//@Test 
public void poolTest() throws Exception { 
 logger.info("jedisPool test"); 
 JedisPoolConfig config = new JedisPoolConfig(); 
 	config.setMaxTotal(12); 
 JedisPool pool = new JedisPool(config, "localhost", 6379); 
 Jedis jedis = pool.getResource(); 
// jedis.set("test", "hoge"); 
 jedis.rpush("test", "1"); 
 jedis.rpush("test", "2"); 
 jedis.rpush("test", "3"); 
 jedis.rpush("test", "4"); 
 jedis.rpush("test", "5"); 
 jedis.rpush("test", "6"); 
 logger.info("result:" + jedis.lrange("test", 0, -1)); 
} 
/**
 * 単体テスト 
 * @throws Exception 
 */ 
//@Test 
public void singleTest() throws Exception { 
 logger.info("jedis test"); 
 Jedis jedis = null; 
 try { 
  jedis = new Jedis("localhost", 6379); 
//  logger.info("result:" + jedis.get("test")); 
 } 
 finally { 
  if(jedis != null) { 
   try { 
    jedis.close(); 
   } 
   catch(Exception e) { 
   } 
   jedis = null; 
  } 
 } 
} 
}