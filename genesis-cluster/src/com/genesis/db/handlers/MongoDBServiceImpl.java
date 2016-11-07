package com.genesis.db.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.genesis.db.service.IDBService;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;



//public class MongoDBServiceImpl{

@SuppressWarnings({"rawtypes", "deprecation"})
public class MongoDBServiceImpl implements IDBService {
	public static Logger logger = LoggerFactory.getLogger("MongoDBServiceImpl"); 
	
	MongoClient mongoClient ;
	DB db;
	DBCollection table;
	//static MongoClientPool pool;
	
	/*
	
	public static synchronized MongoClient getMongoClientConnection() {
		
			if (mongoClient == null) {
				try{
					mongoClient = new MongoClient("localhost", 27017);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			 	//db = mongoClient.getDB("genessis");
				// gets collection user..similar to table
				//table = db.getCollection("dataStore");
				
			}
		
		return mongoClient;
	}

*/

	public MongoDBServiceImpl() throws  UnknownHostException {
		/*mongoClient = new MongoClient("localhost", 27017);
		db = mongoClient.getDB("genessis");
		table = db.getCollection("dataStore");
		*/
	}


	public byte[] serialize(Object obj) throws IOException {
		try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
			try (ObjectOutputStream o = new ObjectOutputStream(b)) {
				o.writeObject(obj);
			}
			return b.toByteArray();
		}
	}

	public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
			try (ObjectInputStream o = new ObjectInputStream(b)) {
				return o.readObject();
			}
		}
	}

	
	public boolean containsKey(String key) { 
		
		// TODO Auto-generated method stub
		String result = null;
		String strKey = "";
		try {
			logger.info("going to get client ---- ");
			mongoClient = new MongoClient("localhost", 27017);
			logger.info("Got to client ---- ");
			//mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDB("genesis");
			table = db.getCollection("dataStore");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("key", key);
			DBCursor cursor = this.table.find(searchQuery);
			while (cursor.hasNext()) {
				DBObject nextDocument = cursor.next();
				result = nextDocument.toString();
				strKey = (String) nextDocument.get("key");
			}
			mongoClient.close();
		}
		catch (Exception e1) {
			e1.printStackTrace();
		}
		if(!strKey.equals("")) 
		{
			return true;
		}
		return false;
	}



	/**
	 * Retrieves data stored at the key
	 * 
	 * @param key String value given for a string
	 * @return map HashMap stored corresponding to the key with chunkIds and values
	 */
	public Map<Integer, byte[]> get(String key) {
		// TODO Auto-generated method stub
		Map<Integer, byte[]> chunkDataMap = new HashMap<Integer, byte[]>();
		String result = "";
		byte[] value = null;
		try {
			logger.info("going to get client ---- ");
			mongoClient = new MongoClient("localhost", 27017);
			logger.info("Got to client ---- ");
			db = mongoClient.getDB("genesis");
			table = db.getCollection("dataStore");
			
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("key", key);
			logger.info("Putting in the search query: "+searchQuery);
			DBCursor cursor = table.find(searchQuery);
			
			//String strTime = "";
			while (cursor.hasNext()) {
				int iChunkId = 0;
				DBObject nextDocument = cursor.next();
				String details = nextDocument.toString();
				logger.info("Value of key.  "+(String) nextDocument.get("key"));
				logger.info("Value of Value.  "+ (byte[])nextDocument.get("value"));
				//iChunkId =  Integer.parseInt( nextDocument.get("chunkId").toString());
				logger.info("Value of ChunkID.  "+ nextDocument.get("chunkId"));
				iChunkId =  (int) nextDocument.get("chunkId");
				//iChunkId = 1;
				value = (byte[]) nextDocument.get("value");
				chunkDataMap.put(iChunkId, value );

			}
			//mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return chunkDataMap;
	}


	public String post(byte[] value) {
		// TODO Auto-generated method stub
		if (value == null) {
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		String key = post(uuid, 1, value);
		return key;
	}
	
	public List<Integer> getChunkIDs(String key) {
		// TODO Auto-generated method stub
		String result = "";
		List<Integer> chunkIds = new ArrayList<Integer>();
		try {
			try {
				logger.info("going to get client ---- ");
				mongoClient = new MongoClient("localhost", 27017);
				logger.info("Got to client ---- ");
				db = mongoClient.getDB("genesis");
				table = db.getCollection("dataStore");
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("key", key);
			DBCursor cursor = table.find(searchQuery);
			//String strTime = "";
			while (cursor.hasNext()) {
				int iChunkId = 0;
				DBObject nextDocument = cursor.next();
				String details = nextDocument.toString();
				iChunkId =  Integer.parseInt((String) nextDocument.get("chunkId"));
				chunkIds.add(iChunkId);

			}
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return chunkIds;
	}

	public String post(String key, int chunkId, byte[] value) {
		// TODO Auto-generated method stub
		boolean done = false;
		String result = "";
        
        // Creating serverside mongoDB which is listening on port 27017 collisionPrevention
        
        try {
    		
        	logger.info("going to get client ---- ");
			mongoClient = new MongoClient("localhost", 27017);
			logger.info("Got to client ---- ");
        	db = mongoClient.getDB("genesis");
    		table = db.getCollection("dataStore");
            BasicDBObject document = new BasicDBObject();
            document.put("keyChunkID", key+chunkId);
            document.put("key", key);
            document.put("chunkId",chunkId );
            document.put("value", value);
            WriteResult resultFinal = table.insert(document);
            // searching the DB to get the document for the given client
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("keyChunkId", key+chunkId);
            DBCursor cursor = table.find(searchQuery);
            while (cursor.hasNext()) {
                DBObject nextDocument = cursor.next();
                String detailsObj = nextDocument.toString();
                result += detailsObj + "\n";
            }
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // sending the registered client information to the client
        //return Response.status(201).entity(result).build();
    
		return "OK";
	}


	@Override
	protected void finalize() throws Throwable {

		if (mongoClient != null) {
			mongoClient.close();
		}
		super.finalize();
	}

	
	public boolean put(String key, int chunkId, byte[] value) {
		// TODO Auto-generated method stub
		if (key == null || value == null) {
			return false;
		}
		try {
			logger.info("going to get client ---- ");
			mongoClient = new MongoClient("localhost", 27017);
			logger.info("Got to client ---- ");
			//mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDB("genesis");
			table = db.getCollection("dataStore");
            BasicDBObject document = new BasicDBObject();
            document.put("keyChunkID", key+chunkId);
            document.put("key", key);
            document.put("chunkId", chunkId);
            document.put("value", value);
            table.insert(document);
            // searching the DB to get the document for the given client
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("keyChunkID", key+chunkId);
            DBCursor cursor = table.find(searchQuery);
            while (cursor.hasNext()) {
                DBObject nextDocument = cursor.next();
                String detailsObj = nextDocument.toString();
            }
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return true;
	}
	
	/*

	public String store(byte[] value) {
		// TODO Auto-generated method stub
		if (value == null) {
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		String key = put(uuid, 1, value);
		return key;
	}

*/
	public boolean delete(String key) {
		// TODO Auto-generated method stub
		try {
			if (containsKey(key)) {
				logger.info("going to get client ---- ");
				mongoClient = new MongoClient("localhost", 27017);
				logger.info("Got to client ---- ");
				//mongoClient = new MongoClient("localhost", 27017);
				db = mongoClient.getDB("genesis");
				table = db.getCollection("dataStore");
				BasicDBObject deleteQuery = new BasicDBObject().append("key", key);
	            table.remove(deleteQuery);
			}
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Response.status(201).entity(result).build();
		
		return false;
	}

	

	public String getDatabaseType() {
		// TODO Auto-generated method stub
		return "MongoDB Database";
	}

}
