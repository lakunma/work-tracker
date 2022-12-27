package com.github.lakunma.worktracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkTrackerApplication {
    private static final Logger log = LoggerFactory.getLogger(WorkTrackerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WorkTrackerApplication.class, args);
    }


}
