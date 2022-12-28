package com.github.lakunma.worktracker.scores;

import com.github.lakunma.worktracker.jira.ticket.JiraTicketService;
import com.github.lakunma.worktracker.workingdates.WorkingDatesService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ScoresCalculator {
    private final int workingDaysWindow;
    private final LocalDate now;

    private final WorkingDatesService workingDatesService;
    private final NormOnDateRepository normOnDateRepository;
    private final JiraTicketService jiraTicketService;

    private final TreeSet<LocalDate> workingDays;

    private final List<NormOnDate> norms;

    public ScoresCalculator(int workingDaysWindow,
                            WorkingDatesService workingDatesService,
                            NormOnDateRepository normOnDateRepository,
                            JiraTicketService jiraTicketService) {

        this.normOnDateRepository = normOnDateRepository;
        this.workingDaysWindow = workingDaysWindow;
        this.jiraTicketService = jiraTicketService;
        this.now = LocalDate.now();
        this.workingDatesService = workingDatesService;

        workingDays = new TreeSet<>(workingDatesService.workingDaysTillNow(workingDaysWindow));
        norms = StreamSupport.stream(normOnDateRepository.findAll().spliterator(), false).toList();
    }

    public List<LocalDate> getDays() {
        LocalDate firstDate = workingDays.first();
        return Stream.iterate(firstDate, curDate -> !curDate.isAfter(now), d -> d.plusDays(1))
                .toList();
    }

    public double getNormOnDate(LocalDate date) {
        if (!workingDatesService.isWorking(date)) {
            return 0;
        }
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

    public double getTotalNorm() {
        return getDays().stream()
                .map(this::getNormOnDate)
                .reduce(0d, Double::sum);
    }

    public double getAvgWorkingWeight() {
        return workingDays.stream()
                .map(this::getWeightOnDate)
                .reduce(0d, Double::sum) / workingDays.size();
    }


    public double getWeightOnDate(LocalDate date) {
        int nWorkingDay = workingDatesService.workingDayFromNow(date);
        double maxWeight = 2.0;

        double pastQ = 1 - (double) nWorkingDay / (workingDaysWindow - 1);
        return maxWeight * pastQ;
    }

    public double getCompletedOnDate(LocalDate date) {
        return jiraTicketService.workhoursOnDate(date);
    }

    public double getTotalCompleted() {
        return getDays().stream()
                .map(this::getCompletedOnDate)
                .reduce(0d, Double::sum);
    }

    public double getWeightedCompletedOnDate(LocalDate date) {
        double workhours = jiraTicketService.workhoursOnDate(date);
        double dateWeight = getWeightOnDate(date);
        return workhours * dateWeight;
    }

    public double getTotalWeightedCompleted(){
        return getDays().stream()
                .map(this::getWeightedCompletedOnDate)
                .reduce(0d, Double::sum);
    }
}
