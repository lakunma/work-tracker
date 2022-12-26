package com.github.lakunma.worktracker;

import com.github.lakunma.worktracker.jira.JiraTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkTrackerApplication {
    private static final Logger log = LoggerFactory.getLogger(WorkTrackerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WorkTrackerApplication.class, args);
    }

    @Autowired
    JiraTicketService jiraTicketService;

    @Bean
    public CommandLineRunner run() throws Exception {
        return args -> {
            var quote = jiraTicketService.fetchTicketsFromJira();

            log.info(quote.toString());
        };
    }
}
