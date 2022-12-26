package com.github.lakunma.worktracker.jira;

import lombok.Data;

@Data
public class JiraTicketDto {
    int id;
    String key;
    JiraTicketFieldsDto fields;
}
