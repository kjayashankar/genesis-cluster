package com.genesis.router.server.edges;

import java.util.concurrent.ConcurrentHashMap;

public class TestCHMap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ConcurrentHashMap<Integer,String> hm = new ConcurrentHashMap();
		
		hm.put(1, "one");
		hm.remove(1);
		hm.put(2, "two:");
		
		System.out.println(hm.keySet());
	}

}
