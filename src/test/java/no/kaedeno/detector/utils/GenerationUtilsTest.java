package no.kaedeno.detector.utils;


import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import static no.kaedeno.detector.utils.GenerationUtils.generateMarkup;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class GenerationUtilsTest {

    private static final String MODERNIZR_URI = "modernizr-2.6.2.min.js";
    private static final String NOSCRIPT_PARAMETER = "nojs";
    private static final String MODERNIZR_COOKIE_ID = "detectorModernizr";

    @Test
    public void modernizrCookieIdIsSetInMarkup() {
        String generatedMarkup = generateMarkup(new MockHttpServletRequest(), MODERNIZR_URI, MODERNIZR_COOKIE_ID, NOSCRIPT_PARAMETER);

        assertThat(generatedMarkup, containsString(MODERNIZR_COOKIE_ID));
    }

    @Test
    public void noscriptParameterIsSetInMarkup() {
        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        String generatedMarkup = generateMarkup(httpServletRequest, MODERNIZR_URI, MODERNIZR_COOKIE_ID, NOSCRIPT_PARAMETER);

        assertThat(generatedMarkup, containsString(httpServletRequest.getRequestURI() + "?" + NOSCRIPT_PARAMETER + "=true"));
    }

    @Test
    public void noscriptParameterDoesNotOverwriteExistingQueryParameters() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setQueryString("?existingParam=test");
        String generatedMarkup = generateMarkup(mockHttpServletRequest, MODERNIZR_URI, MODERNIZR_COOKIE_ID, NOSCRIPT_PARAMETER);

        assertThat(generatedMarkup, containsString(mockHttpServletRequest.getQueryString()));
        assertThat(generatedMarkup, containsString(mockHttpServletRequest.getRequestURI() + "&" + NOSCRIPT_PARAMETER + "=true"));
    }
}
