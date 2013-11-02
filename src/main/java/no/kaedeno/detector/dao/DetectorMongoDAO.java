package no.kaedeno.detector.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import no.kaedeno.detector.domain.UserAgent;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import java.net.UnknownHostException;

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

    public UserAgent save(UserAgent userAgent) {
        WriteResult<UserAgent, String> result = jackColl.save(userAgent);
        return result.getSavedObject();

    }

    public UserAgent update(UserAgent userAgent) {
        WriteResult<UserAgent, String> result = jackColl.updateById(userAgent.getId(), userAgent);
        return result.getSavedObject();
    }

    public void remove(UserAgent userAgent) {
        jackColl.removeById(userAgent.getId());
    }
}
