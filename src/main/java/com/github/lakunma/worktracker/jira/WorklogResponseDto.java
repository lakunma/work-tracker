package com.github.lakunma.worktracker.jira;

import lombok.Data;

import java.util.List;

@Data
public class WorklogResponseDto {
    List<WorklogDto> worklogs;
}
