package no.kaedeno.detector.config;


import no.kaedeno.detector.InterceptorFilter;
import no.kaedeno.detector.dao.DetectorMongoDAO;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class ApplicationContextTest {

    private static ApplicationContext context;

    @BeforeClass
    public static void getContext() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @Test
    public void contextMustBeInstantiated() {
        assertNotNull("The ApplicationContext is null.", context);
    }

    @Test
    public void interceptorFilterMustBeInstantiated() {
        assertNotNull("InterceptorFilter is null.", context.getBean(InterceptorFilter.class));
    }

    @Test
    public void daoMustBeInstantiated() {
        assertNotNull("DAO is null.", context.getBean(DetectorMongoDAO.class));
    }
}
