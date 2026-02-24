package uk.gov.homeoffice.corecloud.dummyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Dummy Service entry point.
 *
 * Throw-away Spring Boot application used to validate all stages of the
 * core-cloud-workflow-maven-docker-actions pipeline. Not intended for
 * deployment beyond pipeline testing.
 */
@SpringBootApplication
public class DummyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DummyServiceApplication.class, args);
    }
}
