package com.github.lakunma.worktracker.jira.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class JiraCategoryService {

    private final JiraCategoryRepository jiraCategoryRepository;
    private final Map<String, JiraCategory> nameToCategory;

    @Autowired
    public JiraCategoryService(JiraCategoryRepository jiraCategoryRepository) {
        this.jiraCategoryRepository = jiraCategoryRepository;
        nameToCategory = StreamSupport.stream(jiraCategoryRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(JiraCategory::getName, Function.identity()));
    }

    public List<JiraCategory> getCategories() {
        return nameToCategory.values().stream().toList();
    }

    private Set<String> jiraKeysForCategory(String categoryName) {
        JiraCategory jiraCategory = nameToCategory.get(categoryName);
        return new HashSet<>(jiraCategory.jiraTicketKeys());
    }

    private Set<String> jiraKeysNotForCategory(String categoryName) {
        return nameToCategory.entrySet().stream()
                .filter(kv -> !kv.getKey().equals(categoryName))
                .flatMap(kv -> kv.getValue().jiraTicketKeys().stream())
                .collect(Collectors.toSet());
    }

    public JiraCategory getCategory(String categoryName) {
        return nameToCategory.get(categoryName);
    }

    public boolean isJiraKeyInsideCategory(String jiraKey, String categoryName) {
        Set<String> jiraKeys = jiraKeysForCategory(categoryName);
        boolean excludeTickets = false;
        if (jiraKeys.isEmpty()) {
            excludeTickets = true;
            jiraKeys = jiraKeysNotForCategory(categoryName);
        }

        return jiraKeys.contains(jiraKey) ^ excludeTickets;

    }
}
