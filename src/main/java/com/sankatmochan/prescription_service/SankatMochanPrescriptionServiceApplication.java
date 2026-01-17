package com.sankatmochan.prescription_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SankatMochanPrescriptionServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(SankatMochanPrescriptionServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SankatMochanPrescriptionServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner startupLogs() {
        return args -> {
            log.info("**********************************************************");
            log.info("********** SANKAT MOCHAN HEALTH PROGRAM STARTED **********");
            log.info("********** PDF MICROSERVICE IS READY          **********");
            log.info("**********************************************************");
            log.info("Service URL: http://localhost:8080/api/v1/prescriptions");
            log.info("Swagger UI: http://localhost:8080/swagger-ui/index.html");
        };
    }
}