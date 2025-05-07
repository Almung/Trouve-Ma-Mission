package com.staffing.service;

import com.staffing.dto.AssignmentDTO;
import com.staffing.model.Assignment;
import com.staffing.model.Collaborator;
import com.staffing.model.Project;
import com.staffing.model.enums.CollaboratorStatus;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.repository.AssignmentRepository;
import com.staffing.repository.CollaboratorRepository;
import com.staffing.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final ProjectRepository projectRepository;

    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(AssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public AssignmentDTO getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .map(AssignmentDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));
    }

    public List<AssignmentDTO> getAssignmentsByCollaboratorId(Long collaboratorId) {
        return assignmentRepository.findByCollaboratorId(collaboratorId).stream()
                .map(AssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AssignmentDTO> getAssignmentsByProjectId(Long projectId) {
        return assignmentRepository.findByProjectId(projectId).stream()
                .map(AssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO) {
        // Vérifier si le collaborateur existe
        Collaborator collaborator = collaboratorRepository.findById(assignmentDTO.getCollaboratorId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + assignmentDTO.getCollaboratorId()));

        // Vérifier si le collaborateur a déjà une affectation active
        List<AssignmentDTO> activeAssignments = assignmentRepository.findByCollaboratorId(assignmentDTO.getCollaboratorId()).stream()
                .filter(a -> !a.getProject().getEndDate().isBefore(LocalDate.now()) 
                        && a.getProject().getStatus() == ProjectStatus.EN_COURS)
                .map(AssignmentDTO::fromEntity)
                .collect(Collectors.toList());
        boolean hasActiveAssignment = activeAssignments.stream()
                .anyMatch(a -> a.getCollaboratorId().equals(assignmentDTO.getCollaboratorId()));
        
        if (hasActiveAssignment) {
            throw new IllegalStateException("Le collaborateur est déjà assigné à un projet actif");
        }

        // Vérifier si le collaborateur est en congé
        if (collaborator.getStatus() == CollaboratorStatus.EN_CONGE) {
            throw new IllegalStateException("Le collaborateur est en congé et ne peut pas être assigné à un projet");
        }

        // Vérifier si le collaborateur est disponible
        if (collaborator.getStatus() != CollaboratorStatus.DISPONIBLE) {
            throw new IllegalStateException("Le collaborateur n'est pas disponible pour une affectation");
        }

        // Vérifier si le projet existe
        Project project = projectRepository.findById(assignmentDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + assignmentDTO.getProjectId()));

        // Vérifier le statut du projet
        if (project.getStatus() == ProjectStatus.TERMINE) {
            throw new IllegalStateException("Impossible d'affecter un collaborateur à un projet terminé");
        }

        // Vérifier si le projet est dans un état valide pour les affectations
        if (project.getStatus() != ProjectStatus.EN_DEMARRAGE && 
            project.getStatus() != ProjectStatus.EN_COURS && 
            project.getStatus() != ProjectStatus.EN_PAUSE) {
            throw new IllegalStateException("Le projet n'est pas dans un état permettant les affectations");
        }

        // Vérifier la taille de l'équipe
        long currentTeamSize = project.getAssignments().size();
        if (currentTeamSize >= project.getTeamSize()) {
            throw new IllegalStateException(
                String.format("L'équipe du projet est déjà complète (Taille maximale: %d)", project.getTeamSize())
            );
        }

        // Créer l'affectation
        Assignment assignment = new Assignment();
        assignment.setCollaborator(collaborator);
        assignment.setProject(project);
        assignment.setRole(assignmentDTO.getRole());
        assignment.setNotes(assignmentDTO.getNotes());

        // Mettre à jour le statut du collaborateur
        collaborator.setStatus(CollaboratorStatus.EN_MISSION);

        // Sauvegarder les modifications
        collaboratorRepository.save(collaborator);
        assignment = assignmentRepository.save(assignment);

        return AssignmentDTO.fromEntity(assignment);
    }

    public AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        Assignment existingAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));

        // Vérifier si le collaborateur est en congé
        Collaborator collaborator = existingAssignment.getCollaborator();
        if (collaborator.getStatus() == CollaboratorStatus.EN_CONGE) {
            throw new IllegalStateException("Le collaborateur est en congé et ne peut pas être modifié dans son affectation");
        }

        // Vérifier le statut du projet
        Project project = existingAssignment.getProject();
        if (project.getStatus() == ProjectStatus.TERMINE) {
            throw new IllegalStateException("Impossible de modifier une affectation sur un projet terminé");
        }

        // Mettre à jour les champs de base
        existingAssignment.setRole(assignmentDTO.getRole());
        existingAssignment.setNotes(assignmentDTO.getNotes());

        // Sauvegarder les modifications
        existingAssignment = assignmentRepository.save(existingAssignment);
        
        return AssignmentDTO.fromEntity(existingAssignment);
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + id));

        // Vérifier le statut du projet
        Project project = assignment.getProject();
        if (project.getStatus() == ProjectStatus.TERMINE || project.getStatus() == ProjectStatus.ANNULE) {
            throw new IllegalStateException("Impossible de supprimer une affectation sur un projet terminé ou annulé");
        }

        // Mettre à jour le statut du collaborateur
        Collaborator collaborator = assignment.getCollaborator();
        collaborator.setStatus(CollaboratorStatus.DISPONIBLE);
        collaboratorRepository.save(collaborator);

        // Supprimer l'affectation
        assignmentRepository.delete(assignment);
    }

    public void removeCollaboratorsFromProject(Long projectId, List<Long> collaboratorIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        if (project.getStatus() == ProjectStatus.TERMINE || project.getStatus() == ProjectStatus.ANNULE) {
            throw new IllegalStateException("Impossible de retirer des collaborateurs d'un projet terminé ou annulé");
        }

        List<Assignment> assignments = assignmentRepository.findByProjectIdAndCollaboratorIdIn(projectId, collaboratorIds);
        
        for (Assignment assignment : assignments) {
            Collaborator collaborator = assignment.getCollaborator();
            collaborator.setStatus(CollaboratorStatus.DISPONIBLE);
            collaboratorRepository.save(collaborator);
            assignmentRepository.delete(assignment);
        }
    }

    public void removeAllCollaboratorsFromProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        if (project.getStatus() == ProjectStatus.TERMINE || project.getStatus() == ProjectStatus.ANNULE) {
            throw new IllegalStateException("Impossible de retirer des collaborateurs d'un projet terminé ou annulé");
        }

        List<Assignment> assignments = assignmentRepository.findByProjectId(projectId);
        
        for (Assignment assignment : assignments) {
            Collaborator collaborator = assignment.getCollaborator();
            collaborator.setStatus(CollaboratorStatus.DISPONIBLE);
            collaboratorRepository.save(collaborator);
            assignmentRepository.delete(assignment);
        }
    }

    public void removeCollaboratorFromAllProjects(Long collaboratorId) {
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + collaboratorId));

        List<Assignment> assignments = assignmentRepository.findByCollaboratorId(collaboratorId);
        
        for (Assignment assignment : assignments) {
            Project project = assignment.getProject();
            if (project.getStatus() != ProjectStatus.TERMINE && project.getStatus() != ProjectStatus.ANNULE) {
                collaborator.setStatus(CollaboratorStatus.DISPONIBLE);
                collaboratorRepository.save(collaborator);
            }
            assignmentRepository.delete(assignment);
        }
    }

    public void removeCollaboratorsFromEndingProjects() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<Assignment> endingAssignments = assignmentRepository.findByProjectEndDateBefore(thirtyDaysFromNow);

        for (Assignment assignment : endingAssignments) {
            Project project = assignment.getProject();
            if (project.getStatus() != ProjectStatus.TERMINE && project.getStatus() != ProjectStatus.ANNULE) {
                Collaborator collaborator = assignment.getCollaborator();
                collaborator.setStatus(CollaboratorStatus.DISPONIBLE);
                collaboratorRepository.save(collaborator);
            }
            assignmentRepository.delete(assignment);
        }
    }

    // Obtenir les statistiques des retraits
    public Map<String, Object> getRemovalStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysFromNow = now.plusDays(30);
        
        // Récupérer toutes les affectations actives
        List<Assignment> activeAssignments = assignmentRepository.findByProjectStatusIn(
            List.of(ProjectStatus.EN_COURS, ProjectStatus.EN_DEMARRAGE, ProjectStatus.EN_PAUSE)
        );
        statistics.put("activeAssignments", activeAssignments.size());
        
        // Calculer le nombre d'affectations qui se terminent dans les 30 jours
        long endingSoon = activeAssignments.stream()
            .filter(a -> a.getProject().getEndDate() != null && 
                        a.getProject().getEndDate().isAfter(now) && 
                        a.getProject().getEndDate().isBefore(thirtyDaysFromNow))
            .count();
        statistics.put("endingSoon", endingSoon);
        
        // Calculer le nombre de collaborateurs qui seront libérés
        long collaboratorsToBeReleased = activeAssignments.stream()
            .filter(a -> a.getProject().getEndDate() != null && 
                        a.getProject().getEndDate().isAfter(now) && 
                        a.getProject().getEndDate().isBefore(thirtyDaysFromNow))
            .map(Assignment::getCollaborator)
            .distinct()
            .count();
        statistics.put("collaboratorsToBeReleased", collaboratorsToBeReleased);
        
        return statistics;
    }

    // Vérifier si un collaborateur peut être retiré
    public boolean canRemoveCollaborator(Long collaboratorId) {
        List<Assignment> assignments = assignmentRepository.findByCollaboratorId(collaboratorId);
        return assignments.stream()
                .noneMatch(assignment -> assignment.getProject().getStatus() == ProjectStatus.EN_COURS);
    }

    public List<AssignmentDTO> getActiveAssignments() {
        return assignmentRepository.findByProjectStatusIn(List.of(ProjectStatus.EN_DEMARRAGE, ProjectStatus.EN_COURS, ProjectStatus.EN_PAUSE))
                .stream()
                .map(AssignmentDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 