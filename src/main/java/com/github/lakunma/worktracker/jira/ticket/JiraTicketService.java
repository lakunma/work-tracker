package com.github.lakunma.worktracker.jira.ticket;

import com.github.lakunma.worktracker.jira.JiraConverter;
import com.github.lakunma.worktracker.jira.JiraRestClient;
import com.github.lakunma.worktracker.jira.worklog.WorkLogRepository;
import com.github.lakunma.worktracker.jira.worklog.Worklog;
import com.github.lakunma.worktracker.jira.worklog.WorklogDto;
import com.github.lakunma.worktracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class JiraTicketService {
    private final JiraTicketRepository jiraTicketRepository;
    private final WorkLogRepository workLogRepository;

    private final JiraRestClient jiraRestClient;
    private final Map<LocalDate, List<Worklog>> dateToWorklog = new HashMap<>();

    @Autowired
    JiraTicketService(UserRepository userRepository, JiraTicketRepository jiraTicketRepository, WorkLogRepository workLogRepository) {
        this.jiraTicketRepository = jiraTicketRepository;
        this.workLogRepository = workLogRepository;

        String authCookie = userRepository.findAll().iterator().next().getCookie();
        String jiraUrlBase = userRepository.findAll().iterator().next().getJiraUrlBase();
        jiraRestClient = new JiraRestClient(authCookie, jiraUrlBase);
    }

    public void resetWorklogCache() {
        dateToWorklog.clear();
    }


    public List<JiraTicket> fetchTicketsFromJira(LocalDate startDate) {
        List<JiraTicketDto> jiraTicketDtos = jiraRestClient.requestTicketsUpdatedAfter(startDate);
        Date now = new Date();
        return jiraTicketDtos.stream().map(dto -> JiraConverter.toJiraTicket(dto, now)).toList();
    }

    public double workhoursOnDate(LocalDate date) {
        List<Worklog> worklogsForDate = getWorklogsForDate(date);
        return worklogsToWorkHours(worklogsForDate);
    }

    private List<JiraTicket> calcUpdatedJiraTickets(LocalDate startDate) {
        Map<String, JiraTicket> oldJiraTickets = StreamSupport.stream(jiraTicketRepository.findAll().spliterator(), false).collect(Collectors.toMap(JiraTicket::getJiraKey, Function.identity()));
        List<JiraTicket> freshJiraTickets = fetchTicketsFromJira(startDate);
        return freshJiraTickets.stream().filter(freshTicket -> {
            var oldJiraTicket = oldJiraTickets.get(freshTicket.getJiraKey());
            return oldJiraTicket == null || oldJiraTicket.getUpdatedInJira().before(freshTicket.getUpdatedInJira());
        }).toList();
    }

    public void updateWorkLogs(LocalDate startDate) {
        List<JiraTicket> jiraTicketsToUpdate = calcUpdatedJiraTickets(startDate);

        List<String> jiraKeysToUpdate = jiraTicketsToUpdate.stream().map(JiraTicket::getJiraKey).toList();
        updateWorkLogs(jiraKeysToUpdate);
        jiraTicketRepository.saveAll(jiraTicketsToUpdate);
    }

    private void updateWorkLogs(List<String> jiraKeysToUpdate) {
        List<Worklog> worklogs = jiraKeysToUpdate.stream()
                .flatMap(jiraKey -> requestWorklogsForJiraTicket(jiraKey).stream())
                .toList();

        workLogRepository.saveAll(worklogs);
        //TODO: optimize recalculation of cache (we may update only affected dates)
        resetWorklogCache();
    }

    private List<Worklog> requestWorklogsForJiraTicket(String jiraKey) {
        List<WorklogDto> worklogDtos = jiraRestClient.requestWorklogsAfter(jiraKey);
        return worklogDtos.stream().map(dto -> JiraConverter.toWorklog(dto, jiraKey)).toList();
    }

    private List<Worklog> getWorklogsForDate(LocalDate date) {
        return dateToWorklog.computeIfAbsent(date, newDate -> {
            Date startOfDay = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(newDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            return workLogRepository.findAllByStartedBetween(startOfDay, endOfDay);
        });
    }

    private double worklogsToWorkHours(List<Worklog> worklogs) {
        return worklogs.stream()
                .map(worklog -> (double) worklog.getTimeSpentInSeconds() / 3600)
                .reduce(0d, Double::sum);
    }

    public double workhoursOnDate(LocalDate date, Predicate<String> isJiraKeyGood) {
        List<Worklog> worklogsForDate = getWorklogsForDate(date);
        List<Worklog> filteredWorklogs = worklogsForDate.stream()
                .filter(worklog -> isJiraKeyGood.test(worklog.getJiraKey()))
                .toList();

        return worklogsToWorkHours(filteredWorklogs);
    }

    public List<Worklog> getWorklogsInBetween(Date start, Date end, Predicate<Worklog> worklogInCategory) {
        return workLogRepository.findAllByStartedBetween(start, end).stream()
                .filter(worklogInCategory)
                .toList();
    }
}
