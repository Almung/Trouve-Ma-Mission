package com.staffing.controller;

import com.staffing.dto.ProjectDTO;
import com.staffing.dto.ProjectFilterDTO;
import com.staffing.dto.ProjectStatisticsDTO;
import com.staffing.dto.ProjectSearchCriteria;
import com.staffing.model.Project;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.service.ProjectService;
import com.staffing.service.ProjectSearchService;
import com.staffing.util.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectSearchService projectSearchService;

    @Autowired
    public ProjectController(ProjectService projectService, ProjectSearchService projectSearchService) {
        this.projectService = projectService;
        this.projectSearchService = projectSearchService;
    }

    @GetMapping
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        return ResponseEntity.ok(projectService.getProjectsByStatus(status));
    }

    @GetMapping("/client/{client}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getProjectsByClient(@PathVariable String client) {
        return ResponseEntity.ok(projectService.getProjectsByClient(client));
    }

    @GetMapping("/search")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> searchProjects(@Valid ProjectSearchCriteria criteria) {
        Page<Project> projectPage = projectService.searchProjects(criteria);
        List<ProjectDTO> projects = projectPage.getContent().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHeaders(
                    projectPage, "/api/projects/search"))
                .body(projects);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
        // Validation du statut
        if (projectDTO.getStatus() == null) {
            projectDTO.setStatus(ProjectStatus.EN_DEMARRAGE);
        }

        // Validation des compétences
        if (projectDTO.getSkillNames() == null) {
            projectDTO.setSkillNames(new HashSet<>());
        }

        return ResponseEntity.ok(projectService.createProject(projectDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO projectDTO) {
        // Validation des compétences
        if (projectDTO.getSkillNames() == null) {
            projectDTO.setSkillNames(new HashSet<>());
        }

        return ResponseEntity.ok(projectService.updateProject(id, projectDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getAllProjectsIncludingInactive() {
        return ResponseEntity.ok(projectService.getAllProjectsIncludingInactive());
    }

    @GetMapping("/active")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getActiveProjects() {
        return ResponseEntity.ok(projectService.getActiveProjects());
    }

    @GetMapping("/inactive")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getInactiveProjects() {
        return ResponseEntity.ok(projectService.getInactiveProjects());
    }

    @GetMapping("/in-progress")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectDTO>> getInProgressProjects() {
        return ResponseEntity.ok(projectService.getInProgressProjects());
    }

    @GetMapping("/skill/{skillName}")
    public ResponseEntity<List<ProjectDTO>> getProjectsBySkill(@PathVariable String skillName) {
        return ResponseEntity.ok(projectService.getProjectsBySkill(skillName));
    }

    @GetMapping("/collaborator/{collaboratorId}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByCollaborator(@PathVariable Long collaboratorId) {
        return ResponseEntity.ok(projectService.getProjectsByCollaborator(collaboratorId));
    }

    @GetMapping("/critical")
    public ResponseEntity<List<ProjectDTO>> getCriticalProjects() {
        List<ProjectDTO> projects = projectService.getCriticalProjects().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/by-skills")
    public ResponseEntity<List<ProjectDTO>> getProjectsBySkills(@RequestParam List<String> skills) {
        List<ProjectDTO> projects = projectService.getProjectsBySkills(skills).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/filter")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<Page<ProjectDTO>> filterProjects(@Valid ProjectFilterDTO filter) {
        Page<Project> projectPage = projectSearchService.searchProjects(filter);
        Page<ProjectDTO> dtoPage = projectPage.map(ProjectDTO::fromEntity);
        
        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHeaders(projectPage, "/api/projects/filter"))
                .body(dtoPage);
    }

    @GetMapping("/search/advanced")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<Page<ProjectDTO>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String client,
            @RequestParam(required = false) Set<ProjectStatus> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) Set<Long> requiredSkillIds,
            @RequestParam(required = false) Integer minProgress,
            @RequestParam(required = false) Integer maxProgress,
            @RequestParam(required = false) String projectManager,
            @RequestParam(required = false) Boolean isLate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        return projectSearchService.advancedSearch(
                name, client, statuses, startDateFrom, startDateTo, endDateFrom, endDateTo,
                minBudget, maxBudget, requiredSkillIds, minProgress, maxProgress,
                projectManager, isLate, page, size, sortBy, sortDirection);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> deactivateProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.deactivateProject(id));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> reactivateProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.reactivateProject(id));
    }

    @GetMapping("/statistics")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<ProjectStatisticsDTO> getProjectStatistics() {
        return ResponseEntity.ok(projectService.getProjectStatistics());
    }
} 