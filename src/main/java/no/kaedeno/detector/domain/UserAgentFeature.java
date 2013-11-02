package no.kaedeno.detector.domain;

import java.util.Map;

import static java.util.Collections.emptyMap;

public class UserAgentFeature {

    private boolean supported;
    private Map<String, Boolean> subFeature;

    public UserAgentFeature() {
        this.supported = false;
        this.subFeature = emptyMap();
    }

    public UserAgentFeature(boolean supported) {
        this.supported = supported;
        this.subFeature = emptyMap();
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

    @Override
    public boolean equals(Object obj) {
        UserAgentFeature userAgentFeature = (UserAgentFeature) obj;
        return (this.isSupported() == userAgentFeature.isSupported() && this.getSubFeature().equals(userAgentFeature.getSubFeature()));
    }
}
