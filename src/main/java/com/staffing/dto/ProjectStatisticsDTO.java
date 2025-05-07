package com.staffing.dto;

import java.util.List;
import com.staffing.model.Skill;

public class ProjectStatisticsDTO {
    private int totalProjects;
    private int activeProjects;
    private int completedProjects;
    private int criticalProjects;
    private List<Skill> leastUsedSkills;

    // Getters and setters
    public int getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(int totalProjects) {
        this.totalProjects = totalProjects;
    }

    public int getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(int activeProjects) {
        this.activeProjects = activeProjects;
    }

    public int getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }

    public int getCriticalProjects() {
        return criticalProjects;
    }

    public void setCriticalProjects(int criticalProjects) {
        this.criticalProjects = criticalProjects;
    }

    public List<Skill> getLeastUsedSkills() {
        return leastUsedSkills;
    }

    public void setLeastUsedSkills(List<Skill> leastUsedSkills) {
        this.leastUsedSkills = leastUsedSkills;
    }
} 