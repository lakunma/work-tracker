package com.github.lakunma.worktracker.jira.worklog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
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
