package com.github.lakunma.worktracker.jira.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraTicket {
    @Id
    Long id;
    String jiraKey;
    Date updatedInJira;
    Date lastFetchedFromJira;
}
