package com.github.lakunma.worktracker.jira;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class JiraTicket {
    @Id
    Long id;
    String jiraKey;
    Date updatedInJira;
    Date lastFetchedFromJira;
}
