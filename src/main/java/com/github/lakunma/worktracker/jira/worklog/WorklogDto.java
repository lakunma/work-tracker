package com.github.lakunma.worktracker.jira.worklog;

import lombok.Data;

import java.util.Date;

@Data
public class WorklogDto {
    long id;
    String comment;
    Date created;
    Date started;
    long timeSpentSeconds;
    WorkLogAuthorDto author;
    WorkLogAuthorDto updateAuthor;
    String issueId;
}
