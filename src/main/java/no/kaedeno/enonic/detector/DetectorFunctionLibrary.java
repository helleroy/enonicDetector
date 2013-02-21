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
	public Document userAgentToDocument(UserAgent userAgent) {
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

	public static String findFamily(UserAgent userAgent, String jsonFileName) {
		Logger log = Logger.getLogger("DARKSIDE");

		Scanner sc;
		File file;
		try {
			file = new File(jsonFileName);
			sc = new Scanner(file);
			log.info("FILE IS EXTERNAL");
		} catch (FileNotFoundException e) {
			log.info("FILE IS INTERNAL");
			InputStream is = DetectorFunctionLibrary.class.getClassLoader().getResourceAsStream(
					"/" + jsonFileName);
			sc = new Scanner(is);
		}
		String jsonFile = sc.useDelimiter("\\Z").next();
		sc.close();

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonObject = mapper.readTree(jsonFile);

			// Iterate over each family
			Iterator<Entry<String, JsonNode>> familyIterator = jsonObject.fields();
			while (familyIterator.hasNext()) {
				Entry<String, JsonNode> family = familyIterator.next();
				String familyName = family.getKey();
				JsonNode familyFeatures = family.getValue();

				log.info("FAMILY FIELD: " + family.toString());

				// Iterate over each feature test in the family
				Iterator<Entry<String, JsonNode>> familyFeatureIterator = familyFeatures.fields();
				while (familyFeatureIterator.hasNext()) {
					Entry<String, JsonNode> feature = familyFeatureIterator.next();
					String featureName = feature.getKey();
					JsonNode featureValue = feature.getValue();

					log.info("FEATURE FIELD: " + feature.toString());
					
					if (!featureValue.isObject()) {
						log.info("FEATURE VALUE IS NOT OBJECT");
						
						// TEST THE SIMPLE VALUE EITHER BOOLEAN OR STRING
					} else {
						// Iterate over each sub feaure test in the feature
						Iterator<Entry<String, JsonNode>> familySubFeatureIterator = featureValue
								.fields();
						while(familySubFeatureIterator.hasNext()) {
							Entry<String, JsonNode> subFeature = familySubFeatureIterator.next();
							String subFeatureName = subFeature.getKey();
							JsonNode subFeatureValue = subFeature.getValue();
							
							log.info("SUB FEATURE FIELD: " + subFeature.toString());
						}
					}
				}
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
