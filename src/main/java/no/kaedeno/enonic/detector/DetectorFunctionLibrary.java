package no.kaedeno.enonic.detector;

import java.io.IOException;
import java.io.StringReader;
import java.net.UnknownHostException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.InputSource;

import com.enonic.cms.api.plugin.PluginConfig;
import com.enonic.cms.api.plugin.PluginEnvironment;
import com.mongodb.DBObject;

public class DetectorFunctionLibrary {

	private PluginConfig pluginConfig = null;
	private PluginEnvironment pluginEnvironment = null;

	private MongoDetectorDAO dao = null;

	private String mongoURI = null;
	private int mongoPort = -1;
	private String mongoName = null;
	private String mongoCollection = null;

	/**
	 * Gets all detected features of the requesting user agent from the database 
	 * as an XML document.
	 * 
	 * @return a JDOM XML Document
	 */
	public Document getUAFeaturesXML() {

		// Database connection
		try {
			setMongoPreferences();
			dao = new MongoDetectorDAO(mongoURI, mongoPort, mongoName, mongoCollection);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		String userAgent = pluginEnvironment.getCurrentRequest().getHeader("User-Agent");
		DBObject result = dao.findOne("userAgent", userAgent);
		
		// Build the JDOM XML Document object from the database result
		try {
			return new SAXBuilder().build(new InputSource(new StringReader(mongoToXML(result))));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Converts a MongoDB DBObject to an XML-formatted String
	 * 
	 * @param mongo the MongoDB DBObject 
	 * @return an XML-formatted String
	 */
	private String mongoToXML(DBObject mongo) {
		try {
			return XML.toString(new JSONObject(mongo.toString()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setMongoPreferences() {
		this.mongoURI = (String) pluginConfig.get("mongodb.uri");
		this.mongoPort = Integer.parseInt(pluginConfig.get("mongodb.port"));
		this.mongoName = (String) pluginConfig.get("mongodb.dbname");
		this.mongoCollection = (String) pluginConfig.get("mongodb.collection");
	}
	
	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	public void setPluginEnvironment(PluginEnvironment pluginEnvironment) {
		this.pluginEnvironment = pluginEnvironment;
	}
}
