package no.kaedeno.enonic.detector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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

		try {
			// Marshal the UserAgent result to XML
			JAXBContext context = JAXBContext.newInstance(UserAgent.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(result, stringWriter);

			// Build the JDOM XML Document object from the marshalled result
			return new SAXBuilder()
					.build(new InputSource(new StringReader(stringWriter.toString())));

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
	 * Gets the user agent family of the requesting user agent from as a string.
	 * The family is decided by the user-defined family definition JSON
	 * 
	 * @return the user agent family as a string
	 */
	public String getUAFamily() {
		String familiesJSONFile = (String) pluginConfig.get("families.uri");
		String familiesJSON;

		File file = new File(familiesJSONFile);
		Scanner sc;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream("/" + familiesJSONFile);
			sc = new Scanner(is);
		}
		familiesJSON = sc.useDelimiter("\\Z").next();
		sc.close();

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonObject = mapper.readTree(familiesJSON);
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
