package com.github.lakunma.worktracker.jira;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface WorkLogRepository extends CrudRepository<Worklog, Integer> {
    List<Worklog> findAllByStartedBetween(Date from, Date to);
}
