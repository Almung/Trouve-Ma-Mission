package com.staffing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardDTO {
    private int totalActiveProjects;
    private int totalCollaborators;
    private int recentProjectUpdates;
    private int overdueProjects;
    private int newAssignments;
    private double overallProjectProgress;
} 