package no.kaedeno.enonic.detector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.enonic.cms.api.plugin.PluginConfig;
import com.enonic.cms.api.plugin.PluginEnvironment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DetectorFunctionLibrary {

	private static final String DEFAULT_FAMILY = "default";

	private PluginConfig pluginConfig = null;
	private PluginEnvironment pluginEnvironment = null;

	private DetectorDAO<UserAgent> dao = null;

	/**
	 * Gets all detected features of the requesting user agent from the database
	 * as an XML document.
	 * 
	 * @return a JDOM XML Document
	 */
	public Document getUAFeaturesXML() {
		String userAgent = pluginEnvironment.getCurrentRequest().getHeader("User-Agent");
		UserAgent result = dao.findOne("userAgent", userAgent);

		return userAgentToDocument(result);
	}

	/**
	 * Gets the user agent family of the requesting user agent as a string. The
	 * family is decided by the user-defined family definition JSON
	 * 
	 * @return the user agent family as a string
	 */
	public String getUAFamily() {
		String userAgent = pluginEnvironment.getCurrentRequest().getHeader("User-Agent");
		UserAgent result = dao.findOne("userAgent", userAgent);

		String jsonFileName = (String) pluginConfig.get("families.uri");

		return findFamily(result, jsonFileName);
	}

	/**
	 * Marshals a UserAgent object into an XML Document object
	 * 
	 * @param userAgent
	 *            the UserAgent object
	 * @return the marshalled UserAgent object as an XML Document
	 */
	private Document userAgentToDocument(UserAgent userAgent) {
		try {
			// Marshal the UserAgent result to XML
			JAXBContext context = JAXBContext.newInstance(UserAgent.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			JAXBElement<UserAgent> rootElement = new JAXBElement<UserAgent>(new QName(
					UserAgent.class.getSimpleName()), UserAgent.class, userAgent);

			marshaller.marshal(rootElement, stringWriter);

			// Build the JDOM XML Document object from the marshalled result
			Document document = new SAXBuilder().build(new InputSource(new StringReader(
					stringWriter.toString())));
			document.setDocType(new DocType(UserAgent.class.getSimpleName()));

			return document;

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads the JSON family file specified in the plugin config and attempts to
	 * find a match with the current user agent object
	 * 
	 * @param userAgent
	 *            the current user agent object
	 * @param jsonFileName
	 *            the file name of the JSON family file
	 * @return the best matching family as a string
	 */
	public static String findFamily(UserAgent userAgent, String jsonFileName) {
		Logger log = Logger.getLogger("DARKSIDE");

		// Read the JSON families file. Reads from different sources depending
		// on the file being the default contained in the project or an
		// external, user-defined file.
		Scanner sc;
		File file;
		try {
			file = new File(jsonFileName);
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			InputStream is = DetectorFunctionLibrary.class.getClassLoader().getResourceAsStream(
					"/" + jsonFileName);
			sc = new Scanner(is);
		}
		String jsonFile = sc.useDelimiter("\\Z").next();
		sc.close();

		// Find a match in the JSON family object
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonObject = mapper.readTree(jsonFile);

			String bestFitFamily = DetectorFunctionLibrary.DEFAULT_FAMILY;
			int mostFieldsMatched = 0;

			String currentFamily = bestFitFamily;
			int currentFieldsMatched = 0;

			// Iterate over each family
			Iterator<Entry<String, JsonNode>> familyIterator = jsonObject.fields();
			while (familyIterator.hasNext()) {

				if (currentFieldsMatched > mostFieldsMatched) {
					bestFitFamily = currentFamily;
					mostFieldsMatched = currentFieldsMatched;
				}

				Entry<String, JsonNode> family = familyIterator.next();
				currentFamily = family.getKey();
				JsonNode familyFeatures = family.getValue();

				currentFieldsMatched = traverseJSONAndCountMatches(familyFeatures, currentFamily,
						userAgent);

				log.info("MATCHED FIELDS FOR " + currentFamily + ": " + currentFieldsMatched);
			}

			return bestFitFamily;

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Recursively traverses a JSON object, tests each value against the current
	 * user agent and counts the number of matches
	 * 
	 * @param json
	 *            the JSON object to traverse
	 * @param parent
	 *            the parent key of the current JSON object
	 * @param userAgent
	 *            the current user agent object
	 * @return the number of fields matched. 0 is returned if any fields do not
	 *         match
	 */
	private static int traverseJSONAndCountMatches(JsonNode json, String parent, UserAgent userAgent) {
		int matchedFields = 0;

		Iterator<Entry<String, JsonNode>> jsonIterator = json.fields();
		while (jsonIterator.hasNext()) {
			Entry<String, JsonNode> jsonEntry = jsonIterator.next();
			String key = jsonEntry.getKey();
			JsonNode value = jsonEntry.getValue();

			if (!value.isObject()) {
				if (testUAFeature(key, value.asText(), parent, userAgent)) {
					matchedFields++;
				} else {
					return 0;
				}
			} else {
				int recursivelyMatched = traverseJSONAndCountMatches(value, key, userAgent);
				if (recursivelyMatched == 0) {
					return 0;
				} else {
					matchedFields += recursivelyMatched;
				}
			}
		}
		return matchedFields;
	}

	/**
	 * Tests a value from the JSON family object against the current user agent
	 * object
	 * 
	 * @param key
	 *            a key from the JSON family object
	 * @param value
	 *            a value from the JSON family object
	 * @param parent
	 *            the parent of the JSON family object with <b>key</b>
	 * @param userAgent
	 *            the current user agent object
	 * @return true if a match is found, false otherwise
	 */
	private static boolean testUAFeature(String key, String value, String parent,
			UserAgent userAgent) {
		if (key.compareToIgnoreCase("uaFamily") == 0) {
			return value.compareToIgnoreCase(userAgent.getUaFamily()) == 0;
		} else if (key.compareToIgnoreCase("uaMajor") == 0) {
			return value.compareToIgnoreCase(userAgent.getUaMajor()) == 0;
		} else if (key.compareToIgnoreCase("uaMinor") == 0) {
			return value.compareToIgnoreCase(userAgent.getUaMinor()) == 0;
		} else if (key.compareToIgnoreCase("osFamily") == 0) {
			return value.compareToIgnoreCase(userAgent.getOsFamily()) == 0;
		} else if (key.compareToIgnoreCase("osMajor") == 0) {
			return value.compareToIgnoreCase(userAgent.getOsMajor()) == 0;
		} else if (key.compareToIgnoreCase("uaMinor") == 0) {
			return value.compareToIgnoreCase(userAgent.getOsMinor()) == 0;
		} else if (key.compareToIgnoreCase("deviceFamily") == 0) {
			return value.compareToIgnoreCase(userAgent.getDeviceFamily()) == 0;
		} else if (key.compareToIgnoreCase("isMobile") == 0) {
			return Boolean.parseBoolean(value) == userAgent.isDeviceIsMobile();
		} else if (key.compareToIgnoreCase("isSpider") == 0) {
			return Boolean.parseBoolean(value) == userAgent.isDeviceIsSpider();
		} else {
			UserAgentFeature userAgentFeature = userAgent.getFeatures().get(parent);
			if (userAgentFeature != null) {
				if (userAgentFeature.getSubFeature() == null) {
					return Boolean.parseBoolean(value) == userAgentFeature.isSupported();
				} else {
					return userAgentFeature.getSubFeature().get(key) == Boolean.parseBoolean(value);
				}
			} else {
				return false;
			}
		}
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	public void setPluginEnvironment(PluginEnvironment pluginEnvironment) {
		this.pluginEnvironment = pluginEnvironment;
	}

	public void setDao(DetectorDAO<UserAgent> dao) {
		this.dao = dao;
	}
}
