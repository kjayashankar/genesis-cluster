package com.genesis.db.handlers;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MongoDBServiceImplTest {

	MongoDBServiceImpl testObj;
	
	@Before
	public void setUp() throws Exception {
		testObj = new MongoDBServiceImpl();
	}
	
	
	@Test
	public void testGetDatabaseVendor() {
		assertEquals("MongoDB Database", testObj.getDatabaseType());
	}
	
	/*
	
	@Test
	public void testDeletionOfKey() {
		Map<Integer, byte[]> testMap = new HashMap<Integer, byte[]>();
		
		String generatedKey = "";
		try {
			testMap.put(0, testObj.serialize("chunkInfo"));
			testMap.put(1, testObj.serialize("data-value-1"));
			generatedKey = testObj.post(testObj.serialize("data-value-1"));
			
			
			//Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
			//boolean isDeleted =  testObj.delete(generatedKey);
			/*for (Integer i : map.keySet()) {
				assertTrue(Arrays.equals(testMap.get(i), map.get(i)));
			}*/
			
			//As it is deleted now
	/*
	assertFalse(testObj.containsKey(generatedKey));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	*/
	
	@Test
	public void testInsertData() {
		//Map<Integer, byte[]> testMap = new HashMap<Integer, byte[]>();
		
		String generatedKey = "";
		try {
			String value = "This is data";
			byte[] byteValue = value.getBytes();
			testObj.put("First_Key",1,byteValue);
			testObj.put("Second_Key",2,byteValue);
			System.out.println("Inserted successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetData() {
		//Map<Integer, byte[]> testMap = new HashMap<Integer, byte[]>();
		
		String generatedKey = "";
		try {
			String value = "This is data";
			byte[] byteValue = value.getBytes();
			System.out.println(testObj.containsKey("First_Key"));
			System.out.println("Retrieved successfully.");

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
