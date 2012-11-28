package no.kaedeno.enonic.detector.enonicDetector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enonic.cms.api.plugin.ext.http.HttpInterceptor;

import ua_parser.Parser;
import ua_parser.Client;

public class Detector extends HttpInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {

		// 1. Look up UA string in cache/database
		// 2. If found:
		// 2.1. Set UA features in context XML
		// 3. If not found:
		// 3.1. Send modernizr tests to client
		// 3.2. Check UA string for useful information
		// 3.3. Store modernizr and UA string result in cache/database
		// 3.4. Set UA features in context XML

		String userAgent = httpServletRequest.getHeader("User-Agent");
		Logger log = Logger.getLogger("Detector");
		log.info("UA String: " + userAgent);

		Parser uaParser = new Parser();
		Client c = uaParser.parse(userAgent);

		log.info("UA Family: " + c.userAgent.family);
		log.info("UA Major version: " + c.userAgent.major);
		log.info("UA Minor version: " + c.userAgent.minor);

		log.info("OS Family: " + c.os.family);
		log.info("OS Major version: " + c.os.major);
		log.info("OS Minor version: " + c.os.minor);

		log.info("Device Family: " + c.device.family);
		log.info("Device is mobile: " + new Boolean(c.device.isMobile).toString());
		log.info("Device is a spider: " + new Boolean(c.device.isSpider).toString());

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws Exception {
		// Do nothing
	}

	private String generateMarkup() {
		// TODO: Generate markup
		return null;
	}

	private boolean sendClientTests(HttpServletResponse httpServletResponse) {
		String markup = this.generateMarkup();
		try {
			PrintWriter w = httpServletResponse.getWriter();
			w.write(markup);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
