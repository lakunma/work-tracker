package com.github.lakunma.worktracker.jira.ticket;

import lombok.Data;

import java.util.Date;

@Data
public class JiraTicketFieldsDto {
    String summary;
    Date created;
    Date updated;
}
