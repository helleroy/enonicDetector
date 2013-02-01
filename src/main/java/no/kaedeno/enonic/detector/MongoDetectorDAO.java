package no.kaedeno.enonic.detector;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MongoDetectorDAO implements DetectorDAO<DBObject, WriteResult, BasicDBObject> {
	
	private MongoClient mongoClient;
	private DB db;
	private DBCollection dbCollection;
	
	public MongoDetectorDAO(String uri, int port, String name, String collection) throws UnknownHostException {		
		this.mongoClient = new MongoClient(uri, port);
		this.db = this.mongoClient.getDB(name);
		this.dbCollection = this.db.getCollection(collection);
	}
	
	public DBObject findOne(String key, String value) {
		return dbCollection.findOne(new BasicDBObject(key, value));
	}
	
	public WriteResult save(BasicDBObject obj) {
		return dbCollection.save(obj);
	}
}
