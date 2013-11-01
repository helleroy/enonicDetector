package no.kaedeno.detector.utils;


import org.junit.Test;

import static no.kaedeno.detector.utils.InterceptorUtils.trueFalse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenerationUtilsTest {

    @Test
    public void shouldReturnTrueWhenGiven1() {
        assertTrue(trueFalse("1"));
    }

    @Test
    public void shouldReturnFalseWhenNotGiven0() {
        assertFalse(trueFalse("0"));
    }

}
