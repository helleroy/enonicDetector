package no.kaedeno.enonic.detector;

import java.net.UnknownHostException;
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

	public void update(UserAgent obj) {
		// TODO Auto-generated method stub
	}

	public void remove(UserAgent obj) {
		// TODO Auto-generated method stub
	}
}
