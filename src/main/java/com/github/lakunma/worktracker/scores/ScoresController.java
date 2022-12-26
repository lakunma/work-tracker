package com.github.lakunma.worktracker.scores;

import com.github.lakunma.worktracker.workingdates.WorkingDatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Controller
public class ScoresController {
    private final WorkingDatesService workingDatesService;
    private final ScoresService scoresService;

    @Autowired
    public ScoresController(WorkingDatesService workingDatesService, ScoresService scoresService) {
        this.workingDatesService=workingDatesService;
        this.scoresService=scoresService;
    }

    @GetMapping("/scores/{days}/status")
    public String status(@PathVariable("days") int days, Model model) {
        List<LocalDate> workingDays = workingDatesService.workingDaysTillNow(days);
        double totalNorm=scoresService.totalNormScores(workingDays);
        model.addAttribute("startDate", workingDays.get(0));
        model.addAttribute("totalNorm", totalNorm);
        return "scores-status";
    }
}
