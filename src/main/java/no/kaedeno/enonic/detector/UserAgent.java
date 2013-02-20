package no.kaedeno.enonic.detector;

import java.util.Map;

import javax.persistence.Id;

public class UserAgent {

	@Id
	private String id;
	
	private String userAgent;

	private String uaFamily;
	private String uaMajor;
	private String uaMinor;

	private String osFamily;
	private String osMajor;
	private String osMinor;

	private String deviceFamily;
	private boolean deviceIsMobile;
	private boolean deviceIsSpider;

	private Map<String, UserAgentFeature> features;

	public UserAgent() {	
	}
	
	public UserAgent(String userAgent, String uaFamily, String uaMajor, String uaMinor,
			String osFamily, String osMajor, String osMinor, String deviceFamily,
			boolean deviceIsMobile, boolean deviceIsSpider, Map<String, UserAgentFeature> features) {
		this.userAgent = userAgent;
		this.uaFamily = uaFamily;
		this.uaMajor = uaMajor;
		this.uaMinor = uaMinor;
		this.osFamily = osFamily;
		this.osMajor = osMajor;
		this.osMinor = osMinor;
		this.deviceFamily = deviceFamily;
		this.deviceIsMobile = deviceIsMobile;
		this.deviceIsSpider = deviceIsSpider;
		this.features = features;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUaFamily() {
		return uaFamily;
	}

	public void setUaFamily(String uaFamily) {
		this.uaFamily = uaFamily;
	}

	public String getUaMajor() {
		return uaMajor;
	}

	public void setUaMajor(String uaMajor) {
		this.uaMajor = uaMajor;
	}

	public String getUaMinor() {
		return uaMinor;
	}

	public void setUaMinor(String uaMinor) {
		this.uaMinor = uaMinor;
	}

	public String getOsFamily() {
		return osFamily;
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

	public String getOsMajor() {
		return osMajor;
	}

	public void setOsMajor(String osMajor) {
		this.osMajor = osMajor;
	}

	public String getOsMinor() {
		return osMinor;
	}

	public void setOsMinor(String osMinor) {
		this.osMinor = osMinor;
	}

	public String getDeviceFamily() {
		return deviceFamily;
	}

	public void setDeviceFamily(String deviceFamily) {
		this.deviceFamily = deviceFamily;
	}

	public boolean isDeviceIsMobile() {
		return deviceIsMobile;
	}

	public void setDeviceIsMobile(boolean deviceIsMobile) {
		this.deviceIsMobile = deviceIsMobile;
	}

	public boolean isDeviceIsSpider() {
		return deviceIsSpider;
	}

	public void setDeviceIsSpider(boolean deviceIsSpider) {
		this.deviceIsSpider = deviceIsSpider;
	}

	public Map<String, UserAgentFeature> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, UserAgentFeature> features) {
		this.features = features;
	}

	@Override
	public String toString() {
		return "UserAgent [userAgent=" + userAgent + ", uaFamily=" + uaFamily + ", uaMajor="
				+ uaMajor + ", uaMinor=" + uaMinor + ", osFamily=" + osFamily + ", osMajor="
				+ osMajor + ", osMinor=" + osMinor + ", deviceFamily=" + deviceFamily
				+ ", deviceIsMobile=" + deviceIsMobile + ", deviceIsSpider=" + deviceIsSpider
				+ ", features=" + features + "]";
	}
}
