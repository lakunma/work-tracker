package com.github.lakunma.worktracker.jira.worklog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(indexes = @Index(columnList = "started"))
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
