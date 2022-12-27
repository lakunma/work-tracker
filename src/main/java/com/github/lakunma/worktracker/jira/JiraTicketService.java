package com.github.lakunma.worktracker.jira;

import com.github.lakunma.worktracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class JiraTicketService {
    private final JiraTicketRepository jiraTicketRepository;
    private final WorkLogRepository workLogRepository;

    private final JiraRestClient jiraRestClient;

    @Autowired
    JiraTicketService(UserRepository userRepository, JiraTicketRepository jiraTicketRepository, WorkLogRepository workLogRepository) {
        this.jiraTicketRepository = jiraTicketRepository;
        this.workLogRepository = workLogRepository;

        String authCookie = userRepository.findAll().iterator().next().getCookie();
        String jiraUrlBase = userRepository.findAll().iterator().next().getJiraUrlBase();
        jiraRestClient = new JiraRestClient(authCookie, jiraUrlBase);
    }


    public List<JiraTicket> fetchTicketsFromJira(LocalDate startDate) {
        List<JiraTicketDto> jiraTicketDtos = jiraRestClient.requestTicketsUpdatedAfter(startDate);
        Date now = new Date();
        return jiraTicketDtos.stream().map(dto -> JiraConverter.toJiraTicket(dto, now)).toList();
    }

    public double workhoursOnDate(LocalDate date) {
        Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Worklog> workLogsForDate = workLogRepository.findAllByStartedBetween(startOfDay, endOfDay);
        return workLogsForDate.stream().map(worklog -> (double) worklog.timeSpentInSeconds / 3600).reduce(0d, Double::sum);
    }

    private List<JiraTicket> calcUpdatedJiraTickets(LocalDate startDate) {
        Map<String, JiraTicket> oldJiraTickets = StreamSupport.stream(jiraTicketRepository.findAll().spliterator(), false).collect(Collectors.toMap(JiraTicket::getJiraKey, Function.identity()));
        List<JiraTicket> freshJiraTickets = fetchTicketsFromJira(startDate);
        return freshJiraTickets.stream().filter(freshTicket -> {
            var oldJiraTicket = oldJiraTickets.get(freshTicket.jiraKey);
            return oldJiraTicket == null || oldJiraTicket.getUpdatedInJira().before(freshTicket.updatedInJira);
        }).toList();
    }

    public void updateWorkLogs(LocalDate startDate) {
        List<JiraTicket> jiraTicketsToUpdate = calcUpdatedJiraTickets(startDate);

        List<String> jiraKeysToUpdate = jiraTicketsToUpdate.stream().map(JiraTicket::getJiraKey).toList();
        updateWorkLogs(jiraKeysToUpdate, startDate);
        jiraTicketRepository.saveAll(jiraTicketsToUpdate);
    }

    private void updateWorkLogs(List<String> jiraKeysToUpdate, LocalDate startDate) {
        List<Worklog> worklogs = jiraKeysToUpdate.stream().flatMap(jiraKey -> requestWorklogsAfter(jiraKey, startDate).stream()).toList();

        workLogRepository.saveAll(worklogs);
    }

    private List<Worklog> requestWorklogsAfter(String jiraKey, LocalDate startDate) {
        List<WorklogDto> worklogDtos = jiraRestClient.requestWorklogsAfter(jiraKey, startDate);
        return worklogDtos.stream().map(dto -> JiraConverter.toWorklog(dto, jiraKey)).toList();
    }
}
