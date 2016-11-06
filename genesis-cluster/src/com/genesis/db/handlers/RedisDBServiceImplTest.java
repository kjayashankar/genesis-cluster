package com.genesis.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class RedisDBServiceImplTest {

	RedisDBServiceImpl testObj;
	
	@Before
	public void setUp() throws Exception {
		testObj = new RedisDBServiceImpl();
	}
	
	@Test
	public void testGetDatabaseVendor() {
		assertEquals("Redis Database", testObj.getDatabaseType());
	}
	
	@Test
	public void testDeletionOfKey() {
		Map<Integer, byte[]> testMap = new HashMap<Integer, byte[]>();
		
		String generatedKey = "";
		try {
			testMap.put(0, testObj.serialize("chunkInfo"));
			testMap.put(1, testObj.serialize("data-value-1"));
			generatedKey = testObj.post(testObj.serialize("data-value-1"));
			
			//Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
			boolean isDeleted =  testObj.delete(generatedKey);
			/*for (Integer i : map.keySet()) {
				assertTrue(Arrays.equals(testMap.get(i), map.get(i)));
			}*/
			
			//As it is deleted now
			assertFalse(testObj.containsKey(generatedKey));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetKey(){
		Map<Integer, byte[]> testMap = new HashMap<Integer, byte[]>();
		testMap = testObj.get("First_One");
		
		System.out.println(testMap.isEmpty());
		
		
		
	}
	
}
