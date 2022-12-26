package com.github.lakunma.worktracker.scores;

import com.github.lakunma.worktracker.jira.JiraTicketService;
import com.github.lakunma.worktracker.workingdates.WorkingDatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class ScoresService {
    NormOnDateRepository normOnDateRepository;
    JiraTicketService jiraTicketService;
    WorkingDatesService workingDatesService;

    @Autowired
    public ScoresService(NormOnDateRepository normOnDateRepository,
                         JiraTicketService jiraTicketService,
                         WorkingDatesService workingDatesService) {
        this.normOnDateRepository = normOnDateRepository;
        this.jiraTicketService = jiraTicketService;
        this.workingDatesService = workingDatesService;
    }

    private double weightOnDate(LocalDate date, int windowInDays) {
        int nWorkingDay = workingDatesService.workingDayFromNow(date);
        double maxWeight = 2.0;

        double pastQ = 1 - (double) nWorkingDay / windowInDays;
        return maxWeight * pastQ;
    }

    public double actualScoreOnDate(LocalDate date, int windowInDays) {
        double workhours = jiraTicketService.workhoursOnDate(date);
        double dateWeight = weightOnDate(date, windowInDays);
        return workhours * dateWeight;
    }

    public double normOnDate(LocalDate date) {
        List<NormOnDate> norms = StreamSupport.stream(normOnDateRepository.findAll().spliterator(), false).toList();
        Optional<NormOnDate> firstLater = norms.stream()
                .filter(nd -> nd.getDate().isAfter(date))
                .findFirst();
        List<NormOnDate> reversedNorms = new ArrayList<>(norms);
        Collections.reverse(reversedNorms);

        Optional<NormOnDate> lastBefore = reversedNorms.stream()
                .filter(nd -> nd.getDate().isBefore(date)).findFirst();

        if (firstLater.isEmpty() && lastBefore.isEmpty()) {
            return 0;
        }

        if (firstLater.isEmpty() || lastBefore.isEmpty()) {
            return firstLater.orElseGet(() -> lastBefore.get()).getNorm();
        }

        //both exists -> do linear interpolation
        LocalDate before = lastBefore.get().getDate();
        LocalDate after = firstLater.get().getDate();
        long period = ChronoUnit.DAYS.between(before, after);
        long dt = ChronoUnit.DAYS.between(before, date);

        double q = (double) dt / period;
        double norm = (1 - q) * lastBefore.get().getNorm() + q * firstLater.get().getNorm();


        return norm;
    }

    public double totalNormScores(List<LocalDate> workingDays) {
        return workingDays.stream()
                .map(this::normOnDate)
                .reduce(0d, Double::sum);
    }
}
