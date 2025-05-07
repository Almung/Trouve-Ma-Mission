package com.staffing.dto;

import com.staffing.model.Assignment;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.model.enums.AssignmentRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AssignmentDTO {
    private Long id;
    
    @NotNull(message = "L'ID du collaborateur est requis")
    private Long collaboratorId;
    
    @NotNull(message = "L'ID du projet est requis")
    private Long projectId;
    
    @NotNull(message = "Le rôle est requis")
    private String role;
    
    private String notes;
    
    // Champs enrichis pour l'affichage
    private String collaboratorName;
    private String projectName;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private ProjectStatus projectStatus;
    private boolean projectActive;

    public static AssignmentDTO fromEntity(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setCollaboratorId(assignment.getCollaborator().getId());
        dto.setProjectId(assignment.getProject().getId());
        dto.setRole(assignment.getRole().toString());
        dto.setNotes(assignment.getNotes());
        dto.setCollaboratorName(assignment.getCollaborator().getName());
        dto.setProjectName(assignment.getProject().getName());
        dto.setProjectStartDate(assignment.getProject().getStartDate());
        dto.setProjectEndDate(assignment.getProject().getEndDate());
        dto.setProjectStatus(assignment.getProject().getStatus());
        dto.setProjectActive(assignment.getProject().isActive());
        return dto;
    }

    public Assignment toEntity() {
        Assignment assignment = new Assignment();
        assignment.setId(this.id);
        assignment.setRole(this.role);
        assignment.setNotes(this.notes);
        return assignment;
    }
} 