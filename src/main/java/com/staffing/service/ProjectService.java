package com.staffing.service;

import com.staffing.dto.ProjectDTO;
import com.staffing.dto.ProjectSearchCriteria;
import com.staffing.dto.ProjectStatisticsDTO;
import com.staffing.model.Project;
import com.staffing.model.Assignment;
import com.staffing.model.Skill;
import com.staffing.model.Collaborator;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.model.enums.CollaboratorStatus;
import com.staffing.repository.ProjectRepository;
import com.staffing.repository.AssignmentRepository;
import com.staffing.repository.SkillRepository;
import com.staffing.repository.CollaboratorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final AssignmentRepository assignmentRepository;
    private final SkillRepository skillRepository;
    private final CollaboratorRepository collaboratorRepository;

    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findByActiveTrue().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ProjectDTO getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(ProjectDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    public List<ProjectDTO> getProjectsByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getActiveProjects() {
        return projectRepository.findByActiveTrue().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getInactiveProjects() {
        return projectRepository.findInactiveProjects().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<Project> searchProjects(ProjectSearchCriteria criteria) {
        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), 
                    "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(criteria.getStatus()));
            }

            if (criteria.getStartDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("startDate"), criteria.getStartDateFrom()));
            }

            if (criteria.getStartDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("startDate"), criteria.getStartDateTo()));
            }

            if (criteria.getEndDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("endDate"), criteria.getEndDateFrom()));
            }

            if (criteria.getEndDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("endDate"), criteria.getEndDateTo()));
            }

            if (criteria.getRequiredSkills() != null && !criteria.getRequiredSkills().isEmpty()) {
                predicates.add(root.join("requiredSkills").get("name").in(criteria.getRequiredSkills()));
            }

            if (criteria.getCritical() != null && criteria.getCritical()) {
                predicates.add(cb.lessThan(root.get("endDate"), LocalDate.now()));
                predicates.add(cb.notEqual(root.get("status"), "COMPLETED"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Default values for pagination
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 10;
        
        // Sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "name"); // default sort
        if (criteria.getSortBy() != null && criteria.getSortDirection() != null) {
            Sort.Direction direction = criteria.getSortDirection().equalsIgnoreCase("DESC") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, criteria.getSortBy());
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return projectRepository.findAll(spec, pageable);
    }

    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = projectDTO.toEntity();
        
        // Gérer les compétences
        Set<String> skillsToAdd = new HashSet<>();
        if (projectDTO.getSkillNames() != null) {
            skillsToAdd.addAll(projectDTO.getSkillNames());
        }
        if (projectDTO.getRequiredSkills() != null) {
            skillsToAdd.addAll(projectDTO.getRequiredSkills());
        }
        
        if (!skillsToAdd.isEmpty()) {
            Set<Skill> skills = new HashSet<>();
            for (String skillName : skillsToAdd) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            newSkill.setCategory("Général");
                            return skillRepository.save(newSkill);
                        });
                skills.add(skill);
            }
            project.setSkills(skills);
        } else {
            project.setSkills(new HashSet<>());
        }

        project = projectRepository.save(project);
        return ProjectDTO.fromEntity(project);
    }

    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project project = projectRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        // Mettre à jour les champs du projet
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setClient(projectDTO.getClient());
        project.setProjectManager(projectDTO.getProjectManager());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setTeamSize(projectDTO.getTeamSize());
        project.setStatus(projectDTO.getStatus());
        project.setActive(projectDTO.isActive());
        project.setProgress(projectDTO.getProgress());

        // Gérer les compétences
        Set<String> skillsToAdd = new HashSet<>();
        if (projectDTO.getSkillNames() != null) {
            skillsToAdd.addAll(projectDTO.getSkillNames());
        }
        if (projectDTO.getRequiredSkills() != null) {
            skillsToAdd.addAll(projectDTO.getRequiredSkills());
        }
        
        if (!skillsToAdd.isEmpty()) {
            Set<Skill> skills = new HashSet<>();
            for (String skillName : skillsToAdd) {
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            newSkill.setCategory("Général");
                            return skillRepository.save(newSkill);
                        });
                skills.add(skill);
            }
            project.setSkills(skills);
        } else {
            project.setSkills(new HashSet<>());
        }

        project = projectRepository.save(project);
        return ProjectDTO.fromEntity(project);
    }

    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    public ProjectDTO deactivateProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        
        project.setActive(false);
        Project savedProject = projectRepository.save(project);
        return ProjectDTO.fromEntity(savedProject);
    }

    public ProjectDTO reactivateProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        
        project.setActive(true);
        project = projectRepository.save(project);

        // Réactiver toutes les affectations associées
        List<Assignment> assignments = assignmentRepository.findByProjectId(id);
        assignmentRepository.saveAll(assignments);

        return ProjectDTO.fromEntity(project);
    }

    public List<ProjectDTO> getProjectsByClient(String client) {
        return projectRepository.findByClient(client).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getProjectsBySkill(String skillName) {
        return projectRepository.findBySkill(skillName).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProjectDTO> getProjectsByCollaborator(Long collaboratorId) {
        return projectRepository.findByCollaboratorId(collaboratorId).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getInProgressProjects() {
        return projectRepository.findInProgressProjects().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Project> getCriticalProjects() {
        return projectRepository.findCriticalProjects(LocalDate.now());
    }

    public List<Project> getProjectsBySkills(List<String> skills) {
        return projectRepository.findBySkills(skills);
    }

    // Ajouter une méthode pour obtenir tous les projets (actifs et inactifs)
    public List<ProjectDTO> getAllProjectsIncludingInactive() {
        return projectRepository.findAll().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les projets actifs par statut
    public List<ProjectDTO> getActiveProjectsByStatus(ProjectStatus status) {
        return projectRepository.findActiveProjectsByStatusAndActive(status).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les projets actifs par client
    public List<ProjectDTO> getActiveProjectsByClient(String client) {
        return projectRepository.findActiveProjectsByClient(client).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Désactiver plusieurs projets en masse
    public void deactivateProjects(List<Long> projectIds) {
        for (Long id : projectIds) {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
            
            // Vérifier s'il y a des affectations actives
            List<Assignment> activeAssignments = assignmentRepository.findByProjectId(id);
            if (!activeAssignments.isEmpty()) {
                throw new IllegalStateException("Impossible de désactiver le projet " + project.getName() + " car il a des affectations actives");
            }
            
            project.setActive(false);
            projectRepository.save(project);
        }
    }

    // Réactiver plusieurs projets en masse
    public void reactivateProjects(List<Long> projectIds) {
        for (Long id : projectIds) {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
            
            project.setActive(true);
            projectRepository.save(project);
        }
    }

    // Obtenir les statistiques des projets actifs/inactifs
    public Map<String, Long> getProjectStatusStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        List<Project> allProjects = projectRepository.findAll();
        
        statistics.put("total", (long) allProjects.size());
        statistics.put("active", allProjects.stream().filter(Project::isActive).count());
        statistics.put("inactive", allProjects.stream().filter(p -> !p.isActive()).count());
        
        return statistics;
    }

    public void assignCollaborator(Long projectId, Long collaboratorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + collaboratorId));

        // Vérifier si le projet est actif
        if (!project.isActive()) {
            throw new IllegalStateException("Impossible d'affecter un collaborateur à un projet inactif");
        }

        // Vérifier si le collaborateur est actif
        if (!collaborator.isActive()) {
            throw new IllegalStateException("Impossible d'affecter un collaborateur inactif");
        }

        // Vérifier si le collaborateur est déjà affecté à un autre projet actif
        List<Assignment> existingAssignments = assignmentRepository.findByCollaboratorIdAndProjectStatus(collaboratorId, ProjectStatus.EN_COURS);
        if (!existingAssignments.isEmpty()) {
            throw new IllegalStateException("Le collaborateur est déjà affecté à un autre projet actif");
        }

        // Créer la nouvelle affectation
        Assignment assignment = new Assignment();
        assignment.setProject(project);
        assignment.setCollaborator(collaborator);

        // Sauvegarder l'affectation
        assignmentRepository.save(assignment);
    }

    public ProjectStatisticsDTO getProjectStatistics() {
        ProjectStatisticsDTO stats = new ProjectStatisticsDTO();
        
        // Calculer le nombre total de projets (tous statuts confondus)
        List<Project> allProjects = projectRepository.findAll();
        stats.setTotalProjects(allProjects.size());
        
        // Calculer le nombre de projets actifs (active = true)
        long activeProjects = allProjects.stream()
            .filter(Project::isActive)
            .count();
        stats.setActiveProjects((int) activeProjects);
        
        // Calculer le nombre de projets terminés
        long completedProjects = allProjects.stream()
            .filter(p -> p.getStatus() == ProjectStatus.TERMINE)
            .count();
        stats.setCompletedProjects((int) completedProjects);
        
        // Calculer le nombre de projets critiques (actifs et en retard)
        LocalDate now = LocalDate.now();
        long criticalProjects = allProjects.stream()
            .filter(p -> p.isActive() && 
                        p.getEndDate() != null && 
                        p.getEndDate().isBefore(now))
            .count();
        stats.setCriticalProjects((int) criticalProjects);

        // Calculer les compétences les moins utilisées
        Map<Skill, Long> skillUsage = new HashMap<>();
        allProjects.stream()
            .flatMap(p -> p.getSkills().stream())
            .forEach(skill -> skillUsage.merge(skill, 1L, Long::sum));

        List<Skill> leastUsedSkills = skillUsage.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        stats.setLeastUsedSkills(leastUsedSkills);
        
        return stats;
    }
} 