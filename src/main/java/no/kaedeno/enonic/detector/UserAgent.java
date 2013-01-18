package no.kaedeno.enonic.detector;

import com.mongodb.BasicDBObject;

public class UserAgent {

	private String userAgent;
	private BasicDBObject ua;
	private BasicDBObject os;
	private BasicDBObject device;
	private BasicDBObject features;

	public UserAgent(String userAgent, BasicDBObject ua, BasicDBObject os, BasicDBObject device,
			BasicDBObject features) {
		this.userAgent = userAgent;
		this.ua = ua;
		this.os = os;
		this.device = device;
		this.features = features;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public BasicDBObject getUa() {
		return ua;
	}

	public void setUa(BasicDBObject ua) {
		this.ua = ua;
	}

	public BasicDBObject getOs() {
		return os;
	}

	public void setOs(BasicDBObject os) {
		this.os = os;
	}

	public BasicDBObject getDevice() {
		return device;
	}

	public void setDevice(BasicDBObject device) {
		this.device = device;
	}

	public BasicDBObject getFeatures() {
		return features;
	}

	public void setFeatures(BasicDBObject features) {
		this.features = features;
	}
	
	public String toString() {
		return userAgent + " " + ua.toString() + os.toString() + device.toString() + features.toString();
	}
}
