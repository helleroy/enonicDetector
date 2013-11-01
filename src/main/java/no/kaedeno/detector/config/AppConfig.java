package no.kaedeno.detector.config;

import no.kaedeno.detector.InterceptorFilter;
import no.kaedeno.detector.dao.DetectorDAO;
import no.kaedeno.detector.dao.DetectorMongoDAO;
import no.kaedeno.detector.domain.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan(basePackages = "no.kaedeno.detector")
@PropertySource("classpath:/no/kaedeno/detector/properties/interceptor.properties")
public class AppConfig {

    @Autowired
    Environment environment;

    @Bean
    public InterceptorFilter interceptorFilter() {
        return new InterceptorFilter();
    }

    @Bean
    public DetectorDAO<UserAgent> mongoDAO() {
        return new DetectorMongoDAO(environment.getProperty("mongodb.host"),
                environment.getProperty("mongodb.port"),
                environment.getProperty("mongodb.dbname"),
                environment.getProperty("mongodb.collection"));
    }
}
