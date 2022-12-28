package com.github.lakunma.worktracker.jira.worklog;

import jakarta.persistence.Column;
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
public class Worklog {
    @Id
    Long id;
    String jiraKey;
    Date started;
    int timeSpentInSeconds;
    String authorEmail;
    @Column(length = 10000)
    String description;
}
