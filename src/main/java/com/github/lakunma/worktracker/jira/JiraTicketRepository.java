package com.github.lakunma.worktracker.jira;

import org.springframework.data.repository.CrudRepository;

public interface JiraTicketRepository extends CrudRepository<JiraTicket, Long> {
}
