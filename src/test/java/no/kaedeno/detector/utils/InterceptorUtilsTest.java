package no.kaedeno.detector.utils;


import no.kaedeno.detector.domain.UserAgentFeature;
import org.junit.Test;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

import static no.kaedeno.detector.utils.InterceptorUtils.getCookie;
import static no.kaedeno.detector.utils.InterceptorUtils.parseCookie;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InterceptorUtilsTest {

    @Test
    public void parsesCookieWithNoSubFeatures() {
        String cookie ="feature1--1|feature2--0|feature3--1|feature4--0";

        Map<String, UserAgentFeature> expectedMap = new HashMap<>();
        expectedMap.put("feature1", new UserAgentFeature(true));
        expectedMap.put("feature2", new UserAgentFeature(false));
        expectedMap.put("feature3", new UserAgentFeature(true));
        expectedMap.put("feature4", new UserAgentFeature(false));
        Map<String, UserAgentFeature> parsedCookie = parseCookie(cookie);

        assertEquals(parsedCookie.size(), 4);
        assertThat(parsedCookie, hasEntry("feature1", new UserAgentFeature(true)));
        assertThat(parsedCookie, hasEntry("feature2", new UserAgentFeature(false)));
        assertThat(parsedCookie, hasEntry("feature3", new UserAgentFeature(true)));
        assertThat(parsedCookie, hasEntry("feature4", new UserAgentFeature(false)));
    }

    @Test
    public void parsesCookieWithSubFeatures() {
        String cookie ="feature1--1|feature2--/subFeature1--1/subFeature2--0|feature3--1|feature4--0";

        Map<String, UserAgentFeature> expectedMap = new HashMap<>();
        expectedMap.put("feature1", new UserAgentFeature(true));

        UserAgentFeature userAgentFeatureWithSubFeatures = new UserAgentFeature();
        Map<String, Boolean> subFeatures = new HashMap<>();
        subFeatures.put("subFeature1", true);
        subFeatures.put("subFeature2", false);
        userAgentFeatureWithSubFeatures.setSubFeature(subFeatures);
        expectedMap.put("feature2", userAgentFeatureWithSubFeatures);

        expectedMap.put("feature3", new UserAgentFeature(true));
        expectedMap.put("feature4", new UserAgentFeature(false));
        Map<String, UserAgentFeature> parsedCookie = parseCookie(cookie);

        assertEquals(parsedCookie.size(), 4);
        assertThat(parsedCookie, hasEntry("feature1", new UserAgentFeature(true)));
        assertThat(parsedCookie, hasEntry("feature2", userAgentFeatureWithSubFeatures));
        assertThat(parsedCookie, hasEntry("feature3", new UserAgentFeature(true)));
        assertThat(parsedCookie, hasEntry("feature4", new UserAgentFeature(false)));

    }

    @Test
    public void getsCookieWithCorrectName() {
        Cookie[] cookies = {
                new Cookie("cookieName1", "cookieValue1"),
                new Cookie("cookieName2", "cookieValue2"),
                new Cookie("cookieName3", "cookieValue3"),
                new Cookie("cookieName4", "cookieValue4")
        };

        Cookie cookie = getCookie(cookies, "cookieName3");
        assertEquals(cookie.getName(), "cookieName3");
        assertEquals(cookie.getValue(), "cookieValue3");
    }
}
