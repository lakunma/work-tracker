package com.github.lakunma.worktracker.scores;

import com.github.lakunma.worktracker.jira.category.JiraCategory;
import com.github.lakunma.worktracker.jira.category.JiraCategoryService;
import com.github.lakunma.worktracker.jira.ticket.JiraTicketService;
import com.github.lakunma.worktracker.workingdates.WorkingDatesService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ScoresCalculator {
    private final int workingDaysWindow;
    private final LocalDate now;
    private final WorkingDatesService workingDatesService;
    private final JiraCategoryService jiraCategoryService;
    private final JiraTicketService jiraTicketService;

    private final TreeSet<LocalDate> workingDays;

    private final List<NormOnDate> norms;

    public ScoresCalculator(int workingDaysWindow, WorkingDatesService workingDatesService, NormOnDateRepository normOnDateRepository, JiraCategoryService jiraCategoryService, JiraTicketService jiraTicketService) {

        this.workingDaysWindow = workingDaysWindow;
        this.jiraCategoryService = jiraCategoryService;
        this.jiraTicketService = jiraTicketService;
        this.now = LocalDate.now();
        this.workingDatesService = workingDatesService;

        workingDays = new TreeSet<>(workingDatesService.workingDaysTillNow(workingDaysWindow));
        norms = StreamSupport.stream(normOnDateRepository.findAll()
                                                         .spliterator(), false)
                             .sorted(Comparator.comparing(NormOnDate::getDate))
                             .toList();
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
                                               .filter(nd -> nd.getDate()
                                                               .isAfter(date))
                                               .findFirst();
        List<NormOnDate> reversedNorms = new ArrayList<>(norms);
        Collections.reverse(reversedNorms);

        Optional<NormOnDate> lastBefore = reversedNorms.stream()
                                                       .filter(nd -> nd.getDate()
                                                                       .isBefore(date))
                                                       .findFirst();

        if (firstLater.isEmpty() && lastBefore.isEmpty()) {
            return 0;
        }

        if (firstLater.isEmpty() || lastBefore.isEmpty()) {
            return firstLater.orElseGet(lastBefore::get)
                             .getNorm();
        }

        //both exists -> do linear interpolation
        LocalDate before = lastBefore.get()
                                     .getDate();
        LocalDate after = firstLater.get()
                                    .getDate();
        long period = ChronoUnit.DAYS.between(before, after);
        long dt = ChronoUnit.DAYS.between(before, date);

        double q = (double) dt / period;

        return (1 - q) * lastBefore.get()
                                   .getNorm() + q * firstLater.get()
                                                              .getNorm();
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

    public double getCompletedOnDate(LocalDate date, String categoryName) {
        Predicate<String> isJiraKeyGood = (String jiraKey) -> jiraCategoryService.isJiraKeyInsideCategory(jiraKey, categoryName);
        return jiraTicketService.workhoursOnDate(date, isJiraKeyGood);
    }

    public double getTotalCompleted() {
        return getDays().stream()
                        .map(this::getCompletedOnDate)
                        .reduce(0d, Double::sum);
    }

    public double getTotalCompleted(String categoryName) {
        return getDays().stream()
                        .map(date -> getCompletedOnDate(date, categoryName))
                        .reduce(0d, Double::sum);
    }


    public double getWeightedCompletedOnDate(LocalDate date) {
        double workhours = jiraTicketService.workhoursOnDate(date);
        double dateWeight = getWeightOnDate(date);
        return workhours * dateWeight;
    }

    public double getTotalWeightedCompleted() {
        return getDays().stream()
                        .map(this::getWeightedCompletedOnDate)
                        .reduce(0d, Double::sum);
    }

    public double getFactorForCategory(String categoryName) {
        double normRatioFactor = getNormRatioFactor();
        JiraCategory category = jiraCategoryService.getCategory(categoryName);
        if (category.isPrimary()) {
            normRatioFactor = 1d;
        }

        return normRatioFactor * category.getFactor();
    }

    public Map<String, Double> getTotalFactoredWeightedCompletedPerCategory() {
        record CatToScore(String categoryName, double score) {
        }

        return getCategories().stream()
                              .map(JiraCategory::getName)
                              .map(categoryName -> {
                                  double score = getDays().stream()
                                                          .map(date -> getCompletedOnDate(date, categoryName) * getWeightOnDate(date))
                                                          .map(completedWeightedHours -> completedWeightedHours * getFactorForCategory(categoryName))
                                                          .reduce(0d, Double::sum);
                                  return new CatToScore(categoryName, score);
                              })
                              .collect(Collectors.toMap(CatToScore::categoryName, CatToScore::score));
    }


    public double getTotalFactoredWeightedCompleted() {
        return getTotalFactoredWeightedCompletedPerCategory().values()
                                                             .stream()
                                                             .reduce(0d, Double::sum);
    }

    public double getRemainingNormalizedWorkHours() {
        double dateWeight = getWeightOnDate(LocalDate.now());
        return (getTotalNorm() - getTotalFactoredWeightedCompleted()) / dateWeight;
    }

    public double getRemainingWorkhoursForToday(String categoryName){
        return getRemainingNormalizedWorkHours()/getNormalizedHoursPerOneHourOfWork(categoryName);
    }
    public double getNormalizedHoursPerOneHourOfWork(String categoryName) {
        JiraCategory category = jiraCategoryService.getCategory(categoryName);
        double dateWeight = getWeightOnDate(LocalDate.now());
        double factor = getFactorForCategory(categoryName);
        double directScore = dateWeight * factor;
        if (!category.isPrimary()) {
            return directScore / dateWeight;
        }

        Double nonPrimaryFactoredScore = getTotalFactoredWeightedCompletedPerCategory().entrySet()
                                                                                       .stream()
                                                                                       .filter(kv -> !jiraCategoryService.getCategory(kv.getKey())
                                                                                                                         .isPrimary())
                                                                                       .map(Map.Entry::getValue)
                                                                                       .reduce(0d, Double::sum);

        double oneHourRatio = 1 / getTotalCompleted(categoryName);
        double indirectScore = nonPrimaryFactoredScore * oneHourRatio;

        return (directScore + indirectScore) / dateWeight;

    }

    public List<JiraCategory> getCategories() {
        return jiraCategoryService.getCategories();
    }

    public List<JiraCategory> getScoringCategories() {
        return jiraCategoryService.getCategories()
                                  .stream()
                                  .filter(c -> c.getMaxScoringThreshold() != null)
                                  .toList();
    }

    public double getAvgCompleted(String categoryName) {
        return getTotalCompleted(categoryName) / workingDaysWindow;
    }

    public double getPenaltyPerCategory(String categoryName) {
        JiraCategory category = jiraCategoryService.getCategory(categoryName);
        double ratio = getAvgCompleted(categoryName) / category.getMaxScoringThreshold();
        return ratio * category.getMaxScoringThresholdQuotient();
    }

    public double getNormRatioFactor() {
        List<Double> qPerCategory = getScoringCategories().stream()
                                                          .map(c -> getPenaltyPerCategory(c.getName()))
                                                          .toList();
        return qPerCategory.stream()
                           .reduce(1d, (a, b) -> a * b);
    }
}
