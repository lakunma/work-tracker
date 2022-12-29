package com.github.lakunma.worktracker.jira;

import com.github.lakunma.worktracker.jira.ticket.JiraTicket;
import com.github.lakunma.worktracker.jira.ticket.JiraTicketDto;
import com.github.lakunma.worktracker.jira.worklog.Worklog;
import com.github.lakunma.worktracker.jira.worklog.WorklogDto;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

@UtilityClass
public class JiraConverter {
    public static JiraTicket toJiraTicket(JiraTicketDto dto, Date jiraFetchTime) {
        return JiraTicket.builder()
                         .id(dto.getId())
                         .jiraKey(dto.getKey())
                         .updatedInJira(dto.getFields()
                                           .getUpdated())
                         .lastFetchedFromJira(jiraFetchTime)
                         .build();
    }

    public static Worklog toWorklog(WorklogDto dto, String jiraKey) {
        Instant instant = Instant.now(); //can be LocalDateTime
        ZoneId systemZone = ZoneId.systemDefault(); // my timezone
        ZoneOffset currentOffsetForMyZone = systemZone.getRules()
                                                      .getOffset(instant);

        Date startedInLocalZone = Date.from(
                new java.sql.Timestamp(dto.getStarted()
                                          .getTime()).toLocalDateTime()
                                                     .minusSeconds(currentOffsetForMyZone.getTotalSeconds())
                                                     .atZone(systemZone)
                                                     .toInstant());

        return Worklog.builder()
                      .authorEmail(dto.getAuthor()
                                      .getEmailAddress())
                      .id(dto.getId())
                      .description(dto.getComment())
                      .started(startedInLocalZone)
                      .jiraKey(jiraKey)
                      .timeSpentInSeconds((int) dto.getTimeSpentSeconds())
                      .build();
    }
}
