package com.github.lakunma.worktracker.jira;

import lombok.Data;

import java.util.List;

@Data
public class JiraTicketsSearchResultDto {
    int total;
    List<JiraTicketDto> issues;
}
