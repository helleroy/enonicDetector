package no.kaedeno.enonic.detector;

import java.util.Map;

public class UserAgentFeature {

	private boolean supported;
	private Map<String, Boolean> subFeature;

	public UserAgentFeature() {
	}
	
	public UserAgentFeature(boolean supported) {
		this.supported = supported;
		this.subFeature = null;
	}
	
	public boolean isSupported() {
		return supported;
	}

	public void setSupported(boolean supported) {
		this.supported = supported;
	}

	public Map<String, Boolean> getSubFeature() {
		return subFeature;
	}

	public void setSubFeature(Map<String, Boolean> subFeature) {
		this.subFeature = subFeature;
	}
	
	@Override
	public String toString() {
		return "UserAgentFeature [supported=" + supported + ", subFeature=" + subFeature + "]";
	}
}
