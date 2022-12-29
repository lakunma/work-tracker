package com.github.lakunma.worktracker.jira;

import com.github.lakunma.worktracker.jira.ticket.JiraTicketDto;
import com.github.lakunma.worktracker.jira.ticket.JiraTicketsSearchResultDto;
import com.github.lakunma.worktracker.jira.worklog.WorklogDto;
import com.github.lakunma.worktracker.jira.worklog.WorklogResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static org.springframework.http.HttpMethod.GET;

public class JiraRestClient {
    private final HttpHeaders headers;
    private final String jiraUrlBase;
    private final RestTemplate restTemplate = new RestTemplate();


    public JiraRestClient(String authCookie, String jiraUrlBase) {
        this.jiraUrlBase = jiraUrlBase;
        headers = new HttpHeaders();
        headers.add("Cookie", authCookie);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    }

    public List<JiraTicketDto> requestTicketsUpdatedAfter(LocalDate startDate) {
        long nDaysFromStartDate = DAYS.between(LocalDate.now(), startDate);
        String restJqlPrefix = "/rest/api/2/search?jql=";
        String jql = MessageFormat.format("project = HITGANP AND updated >= {0}d", nDaysFromStartDate);
        String fieldsFilter = "fields=summary,created,updated";
        String maxResults = "maxResults=200";

        String url = MessageFormat.format("{0}{1}{2}&{3}&{4}",
                jiraUrlBase, restJqlPrefix, jql, fieldsFilter, maxResults);

        var quoteResponse = restTemplate.
                exchange(url, GET,
                        new HttpEntity<String>(headers),
                        JiraTicketsSearchResultDto.class);
        JiraTicketsSearchResultDto searchResult = quoteResponse.getBody();
        if (searchResult == null) {
            return new LinkedList<>();
        }
        return searchResult.getIssues();
    }

    public List<WorklogDto> requestWorklogsAfter(String jiraKey) {
        String restRelPath = MessageFormat.format("/rest/api/2/issue/{0}/worklog", jiraKey);

        String url = MessageFormat.format("{0}{1}", jiraUrlBase, restRelPath);

        var quoteResponse = restTemplate.
                exchange(url, GET,
                        new HttpEntity<String>(headers),
                        WorklogResponseDto.class);

        WorklogResponseDto responseDto = quoteResponse.getBody();

        if (responseDto == null) {
            return emptyList();
        }
        return responseDto.getWorklogs();
    }
}
