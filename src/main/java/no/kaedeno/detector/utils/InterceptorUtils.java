package no.kaedeno.detector.utils;

import no.kaedeno.detector.domain.UserAgentFeature;

import javax.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterceptorUtils {

    /**
     * Parses the value of a cookie with the detector cookie format and builds a
     * UserAgentFeature Map out of it.
     *
     * Adapted from modernizr-server
     *
     * @param cookie
     *            the value of a cookie as a string
     * @return a HashMap containing the information from the cookie or null if
     *         the value is null or has a length of 0
     */
    public static Map<String, UserAgentFeature> parseCookie(String cookie) {
        if (cookie == null || cookie.length() > 0) {

            Map<String, UserAgentFeature> uaFeatures = new LinkedHashMap<String, UserAgentFeature>();

            for (String feature : cookie.split("\\|")) {
                String[] nameValue = feature.split("--", 2);
                String name = nameValue[0];
                String value = nameValue[1];

                UserAgentFeature uaFeature = new UserAgentFeature();

                if (value.charAt(0) == '/') {

                    Map<String, Boolean> uaSubFeatures = new LinkedHashMap<String, Boolean>();

                    for (String subFeature : value.substring(1).split("/")) {
                        nameValue = subFeature.split("--", 2);
                        String subName = nameValue[0];
                        String subValue = nameValue[1];

                        uaSubFeatures.put(subName, trueFalse(subValue));
                    }

                    uaFeature.setSubFeature(uaSubFeatures);
                    uaFeatures.put(name, uaFeature);

                } else {
                    uaFeature.setSupported(trueFalse(value));
                    uaFeatures.put(name, uaFeature);
                }
            }
            return uaFeatures;
        }
        return null;
    }

    /**
     * Decides whether a string gotten from a detector cookie represents the
     * boolean values true or false
     *
     * @param value
     *            the value as a string that is to be checked
     * @return true if the value equals "1", false if not
     */
    public static boolean trueFalse(String value) {
        return value.equals("1") ? true : false;
    }

    /**
     * Gets a specific cookie from an array of cookies
     *
     * @param cookies
     *            the array of cookie
     * @param cookieName
     *            the name of the specific cookie
     * @return the cookie object if the name is present in the array, null if
     *         not
     */
    public static Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null || cookieName == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }


    private InterceptorUtils() {}
}
