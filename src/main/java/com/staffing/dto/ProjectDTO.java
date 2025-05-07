package com.staffing.dto;

import com.staffing.model.Project;
import com.staffing.model.Skill;
import com.staffing.model.enums.ProjectStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String client;
    private String projectManager;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer teamSize;
    private ProjectStatus status;
    private boolean active;
    private double progress;
    private Set<String> skillNames;
    private Set<String> requiredSkills;
    private Set<AssignmentDTO> assignments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProjectDTO fromEntity(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setClient(project.getClient());
        dto.setProjectManager(project.getProjectManager());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setTeamSize(project.getTeamSize());
        dto.setStatus(project.getStatus());
        dto.setActive(project.isActive());
        dto.setProgress(project.getProgress());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        
        // Convert skills to skill names and required skills
        if (project.getSkills() != null) {
            Set<String> skills = project.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());
            dto.setSkillNames(skills);
            dto.setRequiredSkills(skills);
        }
        
        return dto;
    }

    public Project toEntity() {
        Project project = new Project();
        project.setId(this.id);
        project.setName(this.name);
        project.setClient(this.client);
        project.setDescription(this.description);
        project.setStartDate(this.startDate);
        project.setEndDate(this.endDate);
        project.setStatus(convertStatus(this.status));
        project.setProjectManager(this.projectManager);
        project.setTeamSize(this.teamSize);
        project.setActive(this.active);
        project.setProgress(this.progress);
        
        return project;
    }

    private ProjectStatus convertStatus(ProjectStatus status) {
        if (status == null) {
            return ProjectStatus.EN_DEMARRAGE; // Valeur par défaut
        }
        return status;
    }
} 