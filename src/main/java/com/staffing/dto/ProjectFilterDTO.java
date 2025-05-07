package com.staffing.dto;

import com.staffing.model.enums.ProjectStatus;
import java.time.LocalDate;
import java.util.Set;

public class ProjectFilterDTO {
    private String name;
    private String client;
    private Set<ProjectStatus> statuses;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    private Set<Long> requiredSkillIds;
    private Integer minProgress;
    private Integer maxProgress;
    private String projectManager;
    private Boolean isLate;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Set<ProjectStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<ProjectStatus> statuses) {
        this.statuses = statuses;
    }

    public LocalDate getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(LocalDate startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public LocalDate getStartDateTo() {
        return startDateTo;
    }

    public void setStartDateTo(LocalDate startDateTo) {
        this.startDateTo = startDateTo;
    }

    public LocalDate getEndDateFrom() {
        return endDateFrom;
    }

    public void setEndDateFrom(LocalDate endDateFrom) {
        this.endDateFrom = endDateFrom;
    }

    public LocalDate getEndDateTo() {
        return endDateTo;
    }

    public void setEndDateTo(LocalDate endDateTo) {
        this.endDateTo = endDateTo;
    }

    public Set<Long> getRequiredSkillIds() {
        return requiredSkillIds;
    }

    public void setRequiredSkillIds(Set<Long> requiredSkillIds) {
        this.requiredSkillIds = requiredSkillIds;
    }

    public Integer getMinProgress() {
        return minProgress;
    }

    public void setMinProgress(Integer minProgress) {
        this.minProgress = minProgress;
    }

    public Integer getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(Integer maxProgress) {
        this.maxProgress = maxProgress;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
} 