package com.github.lakunma.worktracker.jira;

import lombok.Data;

@Data
public class JiraTicketDto {
    long id;
    String key;
    JiraTicketFieldsDto fields;
}
