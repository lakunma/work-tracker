package com.github.lakunma.worktracker.jira;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Worklog {
    @Id
    Long id;

    LocalDate started;
    int timeSpentInSeconds;
    String authorEmail;
    String description;
}
