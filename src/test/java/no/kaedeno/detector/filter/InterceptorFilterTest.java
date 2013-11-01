package no.kaedeno.detector.filter;


import no.kaedeno.detector.InterceptorFilter;
import no.kaedeno.detector.config.AppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class InterceptorFilterTest {

    @Autowired
    InterceptorFilter interceptorFilter;

    @Test
    public void placeholder() {

    }
}
