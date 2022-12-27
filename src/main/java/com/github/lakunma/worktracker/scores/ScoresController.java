package com.github.lakunma.worktracker.scores;

import com.github.lakunma.worktracker.jira.JiraTicketService;
import com.github.lakunma.worktracker.workingdates.WorkingDatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ScoresController {
    private final WorkingDatesService workingDatesService;
    private final NormOnDateRepository normOnDateRepository;
    private final JiraTicketService jiraTicketService;

    @Autowired
    public ScoresController(WorkingDatesService workingDatesService,
                            NormOnDateRepository normOnDateRepository,
                            JiraTicketService jiraTicketService) {
        this.workingDatesService = workingDatesService;
        this.jiraTicketService = jiraTicketService;
        this.normOnDateRepository = normOnDateRepository;
    }

    @GetMapping("/scores/{days}/status")
    public String status(@PathVariable("days") int days, Model model) {
        ScoresCalculator scoresCalculator = new ScoresCalculator(days, workingDatesService, normOnDateRepository, jiraTicketService);
        jiraTicketService.updateWorkLogs(scoresCalculator.getDays().get(0));
        model.addAttribute("scoresCalculator", scoresCalculator);
        return "scores-status";
    }
}
