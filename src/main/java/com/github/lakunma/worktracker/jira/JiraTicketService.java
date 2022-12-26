package com.github.lakunma.worktracker.jira;

import com.github.lakunma.worktracker.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Service
public class JiraTicketService {
    private final HttpHeaders headers;

    private final UserRepository userRepository;

    @Autowired
    JiraTicketService(UserRepository userRepository) {
        this.userRepository = userRepository;
        headers = new HttpHeaders();
        headers.add("Cookie", userRepository.findAll().iterator().next().getCookie());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public List<JiraTicket> fetchTicketsFromJira() {
        String jiraBaseUrl = userRepository.findAll().iterator().next().getJiraUrlBase();
        String restJqlPrefix = "/rest/api/2/search?jql=";
        String jql = "project = HITGANP AND updated >= -5h";
        String fieldsFilter = "fields=summary,created,updated";

        String url = MessageFormat.format("{0}{1}{2}&{3}", jiraBaseUrl, restJqlPrefix, jql, fieldsFilter);

        var quoteResponse = restTemplate.
                exchange(url, GET,
                        new HttpEntity<String>(headers),
                        JiraTicketsSearchResultDto.class);
        JiraTicketsSearchResultDto searchResult = quoteResponse.getBody();

        return new LinkedList<>();
    }

}
