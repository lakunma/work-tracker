package com.github.lakunma.worktracker.jira;

import lombok.experimental.UtilityClass;

import java.util.Date;

@UtilityClass
public class JiraConverter {
    static JiraTicket toJiraTicket(JiraTicketDto dto, Date jiraFetchTime) {
        return JiraTicket.builder()
                .id(dto.getId())
                .jiraKey(dto.getKey())
                .updatedInJira(dto.fields.getUpdated())
                .lastFetchedFromJira(jiraFetchTime)
                .build();
    }

    public static Worklog toWorklog(WorklogDto dto, String jiraKey) {
        return Worklog.builder()
                .authorEmail(dto.author.emailAddress)
                .id(dto.getId())
                .description(dto.comment)
                .started(dto.started)
                .jiraKey(jiraKey)
                .timeSpentInSeconds((int) dto.timeSpentSeconds)
                .build();
    }
}
