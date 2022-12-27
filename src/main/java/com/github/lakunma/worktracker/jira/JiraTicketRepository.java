package com.github.lakunma.worktracker.jira;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface JiraTicketRepository extends CrudRepository<JiraTicket, Long> {
    //List<JiraTicket> findAllByStarted(LocalDate started);
}
