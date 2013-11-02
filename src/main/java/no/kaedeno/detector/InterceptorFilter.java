package no.kaedeno.detector;

import no.kaedeno.detector.dao.DetectorDAO;
import no.kaedeno.detector.domain.UserAgent;
import no.kaedeno.detector.domain.UserAgentFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.DelegatingFilterProxy;
import ua_parser.Client;
import ua_parser.Parser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static no.kaedeno.detector.utils.GenerationUtils.generateMarkup;
import static no.kaedeno.detector.utils.InterceptorUtils.getCookie;
import static no.kaedeno.detector.utils.InterceptorUtils.parseCookie;

public class InterceptorFilter extends DelegatingFilterProxy {

    private static final String NOSCRIPT_PARAMETER = "nojs";
    private static final String MODERNIZR_COOKIE_ID = "detectorModernizr";

    @Autowired
    public DetectorDAO<UserAgent> dao;

    @Autowired
    private Environment env;

    private static final Logger log = Logger.getLogger(InterceptorFilter.class.getSimpleName());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        // Check if it's not the initial request, in which case we should not
        // query the database
        if (!httpServletRequest.getHeader("accept").contains("text/html")) {
            return;
        }

        String userAgent = httpServletRequest.getHeader("User-Agent");
        UserAgent result = dao.findOne("userAgent", userAgent);

        if (result != null) {
            log.info("Found User-Agent in database: " + result);
            return;
        } else {

            // Send Modernizr tests to client if they haven't been sent already
            Map<String, UserAgentFeature> parsedCookie;

            // Check if the client has responded with not supporting JavaScript
            String nojsParam = httpServletRequest.getParameter(NOSCRIPT_PARAMETER);

            if ("true".equals(nojsParam)) {
                log.info("User-Agent does not support JavaScript.");
                parsedCookie = new LinkedHashMap<String, UserAgentFeature>();
                parsedCookie.put(NOSCRIPT_PARAMETER, new UserAgentFeature(true));
            } else {
                // Check if the client has responded with a client feature cookie.
                // Send the Modernizr tests to the client if not
                Cookie cookie = getCookie(httpServletRequest.getCookies(), MODERNIZR_COOKIE_ID);
                if (cookie == null) {
                    log.info("Unknown User-Agent - sending client-side tests.");
                    sendClientTests(httpServletRequest, httpServletResponse);
                    return;
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
            UserAgent userAgentData = new UserAgent(userAgent,
                    client.userAgent.family,
                    client.userAgent.major,
                    client.userAgent.minor,
                    client.os.family,
                    client.os.major,
                    client.os.minor,
                    client.device.family,
                    client.device.isMobile,
                    client.device.isSpider,
                    parsedCookie);

            result = dao.save(userAgentData);

            log.info("User-Agent features stored in the database: " + result);
        }
    }

    private void sendClientTests(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String markup = generateMarkup(httpServletRequest, env.getProperty("modernizr.uri"), MODERNIZR_COOKIE_ID, NOSCRIPT_PARAMETER);
        try {
            PrintWriter w = httpServletResponse.getWriter();
            w.write(markup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
