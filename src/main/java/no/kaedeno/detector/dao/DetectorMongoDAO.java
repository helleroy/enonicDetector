package no.kaedeno.detector.dao;

import java.net.UnknownHostException;

import no.kaedeno.detector.domain.UserAgent;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class DetectorMongoDAO implements DetectorDAO<UserAgent> {

	private JacksonDBCollection<UserAgent, String> jackColl;

	public DetectorMongoDAO(String host, String port, String dbName, String collectionName) {
		try {
			Mongo mongo = new Mongo(host, Integer.parseInt(port));
			DBCollection dbCollection = mongo.getDB(dbName).getCollection(collectionName);
			this.jackColl = JacksonDBCollection.wrap(dbCollection, UserAgent.class, String.class);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public UserAgent findOne(String key, String value) {
		return jackColl.findOne(new BasicDBObject(key, value));
	}

	public UserAgent save(UserAgent obj) {
		WriteResult<UserAgent, String> result = jackColl.save(obj);
		return result.getSavedObject();

	}

	public UserAgent update(UserAgent obj) {
		WriteResult<UserAgent, String> result = jackColl.updateById(obj.getId(), obj);
		return result.getSavedObject();
	}

	public void remove(UserAgent obj) {
		jackColl.remove(obj);
	}
}
