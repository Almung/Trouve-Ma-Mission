package com.staffing.service;

import com.staffing.dto.DashboardDTO;
import com.staffing.model.*;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private CollaboratorRepository collaboratorRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;

    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();
        
        // Tableau de bord rapide
        List<Project> allProjects = projectRepository.findAll();
        dashboard.setTotalActiveProjects((int) allProjects.stream()
            .filter(Project::isActive)
            .count());
        dashboard.setTotalCollaborators((int) collaboratorRepository.countByActive(true));
        dashboard.setRecentProjectUpdates((int) projectRepository.countRecentUpdates(LocalDateTime.now().minusDays(7)));
        
        // Notifications
        dashboard.setOverdueProjects((int) allProjects.stream()
            .filter(p -> p.isActive() && 
                        p.getEndDate() != null && 
                        p.getEndDate().isBefore(LocalDate.now()))
            .count());
        dashboard.setNewAssignments((int) assignmentRepository.countRecentAssignments(LocalDateTime.now().minusDays(7)));
        
        // Statistiques rapides
        dashboard.setOverallProjectProgress(calculateOverallProgress());
        
        return dashboard;
    }
    
    private double calculateOverallProgress() {
        List<Project> projects = projectRepository.findAll();
        if (projects.isEmpty()) return 0.0;
        
        double totalProgress = projects.stream()
            .filter(p -> p.getStatus() != ProjectStatus.ANNULE)
            .mapToDouble(Project::getProgress)
            .sum();
        
        long activeProjects = projects.stream()
            .filter(p -> p.getStatus() != ProjectStatus.ANNULE)
            .count();
        
        return activeProjects > 0 ? totalProgress / activeProjects : 0.0;
    }
} 