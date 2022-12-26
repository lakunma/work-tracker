package com.github.lakunma.worktracker.scores;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScoresService {
    public double normOnDate(LocalDate date) {
        return 0;
    }

    public double totalNormScores(List<LocalDate> workingDays) {
        return workingDays.stream()
                .map(this::normOnDate)
                .reduce(0d, Double::sum);
    }
}
