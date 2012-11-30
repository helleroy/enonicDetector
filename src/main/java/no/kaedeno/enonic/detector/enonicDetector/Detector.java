package no.kaedeno.enonic.detector.enonicDetector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enonic.cms.api.plugin.PluginConfig;
import com.enonic.cms.api.plugin.PluginEnvironment;
import com.enonic.cms.api.plugin.ext.http.HttpInterceptor;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import ua_parser.Parser;
import ua_parser.Client;

public class Detector extends HttpInterceptor {

	private PluginEnvironment environment = null;
	private PluginConfig pluginConfig = null;

	private MongoClient mongoClient = null;

	private Logger log = Logger.getLogger("Detector");

	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {

		// PLUGIN FLOW:
		// 1. Look up UA string in database
		// 2. If found:
		// 2.1. Set UA features in context XML
		// 3. If not found:
		// 3.1. Send Modernizr tests to client
		// 3.2. Check UA string for useful information
		// 3.3. Store UA Parser and Modernizr results in database
		// 3.4. Set UA features in context XML

		// Database connection
		String mongoURI = (String) pluginConfig.get("mongouri");
		int mongoPort = Integer.parseInt(pluginConfig.get("mongoport"));
		String mongoName = (String) pluginConfig.get("mongoname");
		String mongoCollection = (String) pluginConfig.get("mongocollection");

		setMongoClient(new MongoClient(mongoURI, mongoPort));
		DB db = mongoClient.getDB(mongoName);
		DBCollection coll = getMongoCollection(db, mongoCollection);

		// 1. Look up UA string in database
		String userAgent = httpServletRequest.getHeader("User-Agent");
		DBObject result = coll.findOne(new BasicDBObject("string", userAgent));

		// 2. If found:
		if (result != null) {
			log.info("=== DATABASE ===");
			log.info("Result from DB: " + result);

			// TODO: 2.1 Set UA features in context XML
		}
		// 3. If not found:
		else {
			// TODO: 3.1 Send Modernizr tests to client

			// 3.2 Check UA string for useful information
			Parser uaParser = new Parser();
			Client c = uaParser.parse(userAgent);

			printDebugInfo(c);

			// TODO: 3.3. Store UA Parser and Modernizr results in database
			BasicDBObject userAgentData = new BasicDBObject("string", userAgent)
					.append("ua",
							new BasicDBObject("family", c.userAgent.family).append("major",
									c.userAgent.major).append("minor", c.userAgent.minor))
					.append("os",
							new BasicDBObject("family", c.os.family).append("major", c.os.major)
									.append("minor", c.os.minor))
					.append("device",
							new BasicDBObject("family", c.device.family).append("isMobile",
									c.device.isMobile).append("isSpider", c.device.isSpider))
					.append("capabilities", new ArrayList<String>());

			coll.insert(userAgentData);
			log.info("=== DATABASE ===");
			log.info("Inserted into DB: " + userAgentData);

			// TODO: 3.4. Set UA features in context XML
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {
		// Do nothing
	}

	private String generateMarkup() {
		// TODO: Generate markup
		String modernizr = (String) pluginConfig.get("modernizr");
		return "<!DOCTYPE html>" + 
				"<html>" + 
				"<head>" + 
				"<meta charset='utf-8'>" + 
				"<title>Modernizr Test</title>" + 
				"<script src='" + modernizr + "'></script>" + 
				"</head>" + 
				"</html>";
	}

	@SuppressWarnings("unused")
	private boolean sendClientTests(HttpServletResponse httpServletResponse) {
		String markup = this.generateMarkup();
		try {
			PrintWriter w = httpServletResponse.getWriter();
			w.write(markup);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void printDebugInfo(Client c) {
		log.info("=== UA PARSER RESULT ===");
		log.info("UA Family: " + c.userAgent.family);
		log.info("UA Major version: " + c.userAgent.major);
		log.info("UA Minor version: " + c.userAgent.minor);

		log.info("OS Family: " + c.os.family);
		log.info("OS Major version: " + c.os.major);
		log.info("OS Minor version: " + c.os.minor);

		log.info("Device Family: " + c.device.family);
		log.info("Device is mobile: " + new Boolean(c.device.isMobile).toString());
		log.info("Device is a spider: " + new Boolean(c.device.isSpider).toString());

		log.info("=== SESSION ===");
		if (environment != null) {
			Enumeration<?> attNames = environment.getCurrentSession().getAttributeNames();
			while (attNames.hasMoreElements()) {
				log.info("Session attribute name: " + attNames.nextElement());
			}
		} else {
			log.info("SESSION NOT FOUND");
		}
	}

	private DBCollection getMongoCollection(DB db, String collectionName) {
		return db.collectionExists(collectionName) ? db.getCollection(collectionName) : db
				.createCollection(collectionName, null);
	}

	public void setEnvironment(PluginEnvironment pluginEnvironment) {
		this.environment = pluginEnvironment;
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}
}
