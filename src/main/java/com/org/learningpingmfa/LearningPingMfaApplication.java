package com.org.learningpingmfa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LearningPingMfaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningPingMfaApplication.class, args);
    }

}
