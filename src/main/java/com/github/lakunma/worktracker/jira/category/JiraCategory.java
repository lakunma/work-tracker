package com.github.lakunma.worktracker.jira.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Entity
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraCategory {
    @Id
    Long id;

    String name;

    @Column(length = 10000)
    String jiraTicketKeysAsCSV;

    Double maxScoringThreshold;
    Double maxScoringThresholdQuotient;
    double factor;

    public List<String> jiraTicketKeys() {
        return Optional.ofNullable(jiraTicketKeysAsCSV).stream()
                .flatMap(csv -> Arrays.stream(csv.split(",")))
                .map(String::strip)
                .filter(w -> !w.isBlank())
                .toList();
    }
}
