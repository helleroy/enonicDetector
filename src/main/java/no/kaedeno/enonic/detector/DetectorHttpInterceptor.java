package no.kaedeno.enonic.detector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ua_parser.Client;
import ua_parser.Parser;

import com.enonic.cms.api.plugin.PluginConfig;
import com.enonic.cms.api.plugin.ext.http.HttpInterceptor;

public class DetectorHttpInterceptor extends HttpInterceptor {

	private static final String NOSCRIPT_PARAMETER = "nojs";
	private static final String MODERNIZR_COOKIE_ID = "detectorModernizr";

	private PluginConfig pluginConfig = null;

	private DetectorDAO<UserAgent> dao = null;

	private Logger log = Logger.getLogger("DetectorHttpInterceptor");

	/**
	 * Handles the request received by the server before it is executed by
	 * Enonic CMS. It is responsible for checking the user-agent string in the
	 * request against the database to find the features and capabilities of the
	 * user-agent making the request. If the string is not present in the
	 * database it sends a Modernizr test suite to the client to check for its
	 * user-agent features, and parses the user-agent string for any useful
	 * information.
	 * <p>
	 * The features and capabilities are written to the database defined by the
	 * plugin properties and Spring context.
	 * 
	 * @param httpServletRequest
	 *            the http request
	 * @param httpServletResponse
	 *            the http response
	 * @return true if the request should be passed to execution in Enonic CMS
	 *         or false if not.
	 */
	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

		// Check if it's not the initial request, in which case we should not
		// query the database
		if (!httpServletRequest.getHeader("accept").contains("text/html")) {
			return true;
		}

		// Look up UA string in database
		String userAgent = httpServletRequest.getHeader("User-Agent");
		UserAgent result = dao.findOne("userAgent", userAgent);

		if (result != null) {

			log.info("Result: " + result);

			return true;
		} else {
			// Send Modernizr tests to client if they haven't been sent already
			Map<String, UserAgentFeature> parsedCookie = null;

			// Check if the client has responded with not supporting JavaScript
			String nojsParam = httpServletRequest
					.getParameter(DetectorHttpInterceptor.NOSCRIPT_PARAMETER);

			if (nojsParam != null && nojsParam.compareTo("true") == 0) {
				parsedCookie = new LinkedHashMap<String, UserAgentFeature>();
				parsedCookie.put(DetectorHttpInterceptor.NOSCRIPT_PARAMETER, new UserAgentFeature(
						true));
			} else {
				// Check if the client has responded with a client feature
				// cookie. Send the Modernizr tests to the client if not
				Cookie cookie = getCookie(httpServletRequest.getCookies(),
						DetectorHttpInterceptor.MODERNIZR_COOKIE_ID);
				if (cookie == null) {
					sendClientTests(httpServletRequest, httpServletResponse);
					return false;
				} else {
					parsedCookie = parseCookie(cookie.getValue());
				}
			}

			// Check UA string for useful information using UA Parser
			Parser uaParser = null;
			try {
				uaParser = new Parser();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Client client = uaParser.parse(userAgent);

			// Store UA Parser and Modernizr results in database
			UserAgent userAgentData = new UserAgent(userAgent, client.userAgent.family,
					client.userAgent.major, client.userAgent.minor, client.os.family,
					client.os.major, client.os.minor, client.device.family, client.device.isMobile,
					client.device.isSpider, parsedCookie);

			result = dao.save(userAgentData);

			log.info("Inserted: " + result);

			return true;
		}
	}

	/**
	 * Handles the request after it has been executed by Enonic CMS.
	 * 
	 * @param httpServletRequest
	 *            the http request
	 * @param httpServletResponse
	 *            the http response
	 */
	@Override
	public void postHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {
	}

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
	private Map<String, UserAgentFeature> parseCookie(String cookie) {
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
	private boolean trueFalse(String value) {
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
	private Cookie getCookie(Cookie[] cookies, String cookieName) {
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

	/**
	 * Generates a string with HTML markup and the appropriate JavaScript code
	 * to run Modernizr tests on the client.
	 * 
	 * @param httpServletRequest
	 *            the http request
	 * @return the generated markup
	 */
	private String generateMarkup(HttpServletRequest httpServletRequest) {
		String modernizrFileName = (String) pluginConfig.get("modernizr.uri");
		String modernizrScript = null;

		// Read the Modernizr file. Reads from different sources depending on
		// the file being the default file contained in the project or an
		// external, user-defined file.
		Scanner sc;
		File file;
		try {
			file = new File(modernizrFileName);
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(
					"/" + modernizrFileName);
			sc = new Scanner(is);
		}
		modernizrScript = sc.useDelimiter("\\Z").next();
		sc.close();

		return "<!DOCTYPE html><html><head><meta charset='utf-8'><script type='text/javascript'>"
				+ "var before = new Date().getTime();" 
				+ modernizrScript + generateCookieJS(false)
				+ "var after = new Date().getTime();alert(after-before);"
				+ "</script></head><body><noscript><meta http-equiv='refresh' content='0; url="
				+ generateNoscriptRedirect(httpServletRequest) + "'></noscript></body></html>";
	}

	/**
	 * Generates the JavaScript code for reading the Modernizr test result
	 * object on the client, and writes the results to a cookie formatted in
	 * key-value pairs.
	 * 
	 * Adapted from modernizr-server
	 * 
	 * @param reload
	 *            true if the script should reload the page after creating the
	 *            cookie, false if not
	 * @return the generated code
	 */
	private String generateCookieJS(boolean reload) {
		String output = "var m=Modernizr,c='';" + "for(var f in m){" + "if(f[0]=='_'){continue;}"
				+ "var t=typeof m[f];" + "if(t=='function'){continue;}" + "c+=(c?'|':'"
				+ DetectorHttpInterceptor.MODERNIZR_COOKIE_ID + "=')+f+'--';" + "if(t=='object'){"
				+ "for(var s in m[f]){" + "c+='/'+s+'--'+(m[f][s]?'1':'0');" + "}" + "}else{"
				+ "c+=m[f]?'1':'0';" + "}" + "}" + "c+=';path=/';" + "try{" + "document.cookie=c;";
		if (reload) {
			output += "document.location.reload();";
		}
		output += "}catch(e){}";

		return output;
	}

	/**
	 * Generates a noscript redirect url, so that browsers without JavaScript
	 * support can be redirected back to the correct page. Adds a GET parameter
	 * to the URL so that the plugin can detect the lack of JavaScript support
	 * 
	 * @param httpServletRequest
	 *            the http request
	 * @return the generated noscript redirect url
	 */
	private String generateNoscriptRedirect(HttpServletRequest httpServletRequest) {
		String url = httpServletRequest.getRequestURL().toString();
		String queryParams = httpServletRequest.getQueryString();
		if (queryParams != null) {
			url += "?" + queryParams + "&" + DetectorHttpInterceptor.NOSCRIPT_PARAMETER + "=true";
		} else {
			url += "?" + DetectorHttpInterceptor.NOSCRIPT_PARAMETER + "=true";
		}
		return url;
	}

	/**
	 * Sends markup containing the necessary JavaScript code to run Modernizr
	 * tests on the client as well as generating a cookie with the test results
	 * that can be sent to the server.
	 * 
	 * @param httpServletResponse
	 *            the http response
	 */
	private void sendClientTests(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {

		String markup = generateMarkup(httpServletRequest);
		try {
			PrintWriter w = httpServletResponse.getWriter();
			w.write(markup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	public void setDao(DetectorDAO<UserAgent> dao) {
		this.dao = dao;
	}
}
