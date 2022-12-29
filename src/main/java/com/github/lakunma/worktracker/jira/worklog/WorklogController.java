package com.github.lakunma.worktracker.jira.worklog;

import com.github.lakunma.worktracker.jira.category.JiraCategoryService;
import com.github.lakunma.worktracker.jira.ticket.JiraTicketService;
import com.github.lakunma.worktracker.workingdates.WorkingDatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Controller
public class WorklogController {
    private final WorkingDatesService workingDatesService;
    private final JiraTicketService jiraTicketService;
    private final JiraCategoryService jiraCategoryService;

    @Autowired
    public WorklogController(WorkingDatesService workingDatesService,
                             JiraTicketService jiraTicketService,
                             JiraCategoryService jiraCategoryService) {
        this.workingDatesService = workingDatesService;
        this.jiraTicketService = jiraTicketService;
        this.jiraCategoryService = jiraCategoryService;
    }

    @GetMapping("/worklog/since-last-daily")
    public String sinceLastDaily(@RequestParam String category, Model model) {
        Function<LocalDate, Date> toMeetingTime =
                (LocalDate localDate) -> Date.from(localDate.atTime(13, 30)
                                                            .atZone(ZoneId.systemDefault())
                                                            .toInstant());
        Date end = toMeetingTime.apply(LocalDate.now());
        Date start = toMeetingTime.apply(workingDatesService.workingDayBefore(2));

        Predicate<Worklog> worklogInCategory =
                (Worklog worklog) -> jiraCategoryService.isJiraKeyInsideCategory(worklog.getJiraKey(), category);

        List<Worklog> worklogs = jiraTicketService.getWorklogsInBetween(start, end, worklogInCategory);

        model.addAttribute("worklogs", worklogs);
        return "worklog/since-last-daily";
    }
}
