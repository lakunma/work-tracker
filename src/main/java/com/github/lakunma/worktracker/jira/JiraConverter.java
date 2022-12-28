package com.github.lakunma.worktracker.jira;

import com.github.lakunma.worktracker.jira.ticket.JiraTicket;
import com.github.lakunma.worktracker.jira.ticket.JiraTicketDto;
import com.github.lakunma.worktracker.jira.worklog.Worklog;
import com.github.lakunma.worktracker.jira.worklog.WorklogDto;
import lombok.experimental.UtilityClass;

import java.util.Date;

@UtilityClass
public class JiraConverter {
    public static JiraTicket toJiraTicket(JiraTicketDto dto, Date jiraFetchTime) {
        return JiraTicket.builder()
                .id(dto.getId())
                .jiraKey(dto.getKey())
                .updatedInJira(dto.getFields().getUpdated())
                .lastFetchedFromJira(jiraFetchTime)
                .build();
    }

    public static Worklog toWorklog(WorklogDto dto, String jiraKey) {
        return Worklog.builder()
                .authorEmail(dto.getAuthor().getEmailAddress())
                .id(dto.getId())
                .description(dto.getComment())
                .started(dto.getStarted())
                .jiraKey(jiraKey)
                .timeSpentInSeconds((int) dto.getTimeSpentSeconds())
                .build();
    }
}
