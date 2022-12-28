package com.github.lakunma.worktracker.jira.ticket;

import org.springframework.data.repository.CrudRepository;

public interface JiraTicketRepository extends CrudRepository<JiraTicket, Long> {
    //List<JiraTicket> findAllByStarted(LocalDate started);
}
