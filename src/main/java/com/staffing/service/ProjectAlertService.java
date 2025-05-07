package com.staffing.service;

import com.staffing.model.Project;
import com.staffing.model.ProjectAlert;
import com.staffing.model.Assignment;
import com.staffing.repository.ProjectAlertRepository;
import com.staffing.repository.ProjectRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ProjectAlertService {
    private final ProjectAlertRepository alertRepository;
    private final ProjectRepository projectRepository;

    public ProjectAlertService(ProjectAlertRepository alertRepository, 
                             ProjectRepository projectRepository) {
        this.alertRepository = alertRepository;
        this.projectRepository = projectRepository;
    }

    @Cacheable(value = "projectAlerts")
    public List<ProjectAlert> getActiveAlerts() {
        return alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
    }

    @Cacheable(value = "highPriorityAlerts")
    public List<ProjectAlert> getHighPriorityAlerts() {
        return alertRepository.findHighPriorityAlerts();
    }

    public List<ProjectAlert> getProjectAlerts(Long projectId) {
        return alertRepository.findByProjectIdAndIsResolvedFalse(projectId);
    }

    @CacheEvict(value = {"projectAlerts", "highPriorityAlerts"}, allEntries = true)
    public ProjectAlert resolveAlert(Long alertId) {
        ProjectAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setResolved(true);
        return alertRepository.save(alert);
    }

    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @CacheEvict(value = {"projectAlerts", "highPriorityAlerts"}, allEntries = true)
    @Transactional
    public void checkForNewAlerts() {
        List<Project> projects = projectRepository.findAll();
        LocalDate now = LocalDate.now();
        
        for (Project project : projects) {
            // Vérifier les deadlines approchantes (7 jours)
            if (project.getEndDate() != null && 
                !project.getStatus().toString().equals("COMPLETED")) {
                long daysUntilDeadline = LocalDate.now().until(project.getEndDate()).getDays();
                
                if (daysUntilDeadline <= 7 && daysUntilDeadline > 0) {
                    createAlert(project, 
                              ProjectAlert.AlertType.DEADLINE_APPROACHING,
                              "Le projet arrive à échéance dans " + daysUntilDeadline + " jours",
                              daysUntilDeadline <= 3 ? 
                                  ProjectAlert.AlertSeverity.HIGH : 
                                  ProjectAlert.AlertSeverity.MEDIUM);
                }
                
                // Projets en retard
                if (project.getEndDate().isBefore(now)) {
                    createAlert(project,
                              ProjectAlert.AlertType.DEADLINE_MISSED,
                              "Le projet a dépassé sa date d'échéance de " + 
                                  now.until(project.getEndDate()).getDays() + " jours",
                              ProjectAlert.AlertSeverity.CRITICAL);
                }
            }


            // Vérifier les ressources
            if (project.getAssignments() != null && project.getSkills() != null) {
                if (project.getAssignments().isEmpty()) {
                    createAlert(project,
                              ProjectAlert.AlertType.RESOURCE_SHORTAGE,
                              "Le projet n'a aucune ressource assignée",
                              ProjectAlert.AlertSeverity.HIGH);
                } else {
                    // Vérifier les compétences manquantes
                    boolean hasSkillGap = project.getSkills().stream()
                            .anyMatch(requiredSkill -> 
                                project.getAssignments().stream()
                                    .map(Assignment::getCollaborator)
                                    .noneMatch(collaborator -> 
                                        collaborator.getSkills().contains(requiredSkill)));
                    
                    if (hasSkillGap) {
                        createAlert(project,
                                  ProjectAlert.AlertType.SKILL_GAP,
                                  "Il manque des compétences requises dans l'équipe projet",
                                  ProjectAlert.AlertSeverity.HIGH);
                    }
                }
            }
        }
    }

    private void createAlert(Project project, 
                           ProjectAlert.AlertType type,
                           String message,
                           ProjectAlert.AlertSeverity severity) {
        // Éviter les doublons d'alertes
        List<ProjectAlert> existingAlerts = alertRepository.findByProjectIdAndIsResolvedFalse(project.getId());
        boolean alertExists = existingAlerts.stream()
                .anyMatch(alert -> alert.getType() == type);
        
        if (!alertExists) {
            ProjectAlert alert = new ProjectAlert();
            alert.setProject(project);
            alert.setType(type);
            alert.setMessage(message);
            alert.setSeverity(severity);
            alert.setResolved(false);
            alertRepository.save(alert);
        }
    }
} 