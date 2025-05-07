package com.staffing.service;

import com.staffing.dto.ProjectDTO;
import com.staffing.dto.ProjectFilterDTO;
import com.staffing.model.Project;
import com.staffing.model.Skill;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.repository.ProjectRepository;
import com.staffing.util.PaginationUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProjectSearchService {
    private final ProjectRepository projectRepository;

    public ProjectSearchService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Page<Project> searchProjects(ProjectFilterDTO filter) {
        Specification<Project> spec = buildSpecification(filter);
        Pageable pageable = buildPageable(filter);
        return projectRepository.findAll(spec, pageable);
    }

    private Specification<Project> buildSpecification(ProjectFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtres de base
            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), 
                    "%" + filter.getName().toLowerCase() + "%"));
            }

            if (filter.getClient() != null && !filter.getClient().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("client")), 
                    "%" + filter.getClient().toLowerCase() + "%"));
            }

            // Filtres de statut
            if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatuses()));
            }

            // Filtres de dates
            if (filter.getStartDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), 
                    filter.getStartDateFrom()));
            }
            if (filter.getStartDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), 
                    filter.getStartDateTo()));
            }
            if (filter.getEndDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endDate"), 
                    filter.getEndDateFrom()));
            }
            if (filter.getEndDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), 
                    filter.getEndDateTo()));
            }

            // Filtres de progression

            // Filtres de progression
            if (filter.getMinProgress() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("progress"), 
                    filter.getMinProgress()));
            }
            if (filter.getMaxProgress() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("progress"), 
                    filter.getMaxProgress()));
            }

            // Filtre de chef de projet
            if (filter.getProjectManager() != null && !filter.getProjectManager().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("projectManager")), 
                    "%" + filter.getProjectManager().toLowerCase() + "%"));
            }

            // Filtre de compétences requises
            if (filter.getRequiredSkillIds() != null && !filter.getRequiredSkillIds().isEmpty()) {
                Join<Project, Skill> skillJoin = root.join("requiredSkills");
                predicates.add(skillJoin.get("id").in(filter.getRequiredSkillIds()));
            }

            // Filtre de retard
            if (filter.getIsLate() != null && filter.getIsLate()) {
                predicates.add(cb.lessThan(root.get("endDate"), LocalDate.now()));
                predicates.add(cb.notEqual(root.get("status"), "COMPLETED"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Pageable buildPageable(ProjectFilterDTO filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDirection()), filter.getSortBy());
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    public ResponseEntity<Page<ProjectDTO>> advancedSearch(
            String name, String client, Set<ProjectStatus> statuses,
            LocalDate startDateFrom, LocalDate startDateTo,
            LocalDate endDateFrom, LocalDate endDateTo,
            Double minBudget, Double maxBudget,
            Set<Long> requiredSkillIds,
            Integer minProgress, Integer maxProgress,
            String projectManager, Boolean isLate,
            Integer page, Integer size,
            String sortBy, String sortDirection) {

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setName(name);
        filter.setClient(client);
        filter.setStatuses(statuses);
        filter.setStartDateFrom(startDateFrom);
        filter.setStartDateTo(startDateTo);
        filter.setEndDateFrom(endDateFrom);
        filter.setEndDateTo(endDateTo);
        filter.setRequiredSkillIds(requiredSkillIds);
        filter.setMinProgress(minProgress);
        filter.setMaxProgress(maxProgress);
        filter.setProjectManager(projectManager);
        filter.setIsLate(isLate);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);

        Page<Project> projectPage = searchProjects(filter);
        Page<ProjectDTO> dtoPage = projectPage.map(ProjectDTO::fromEntity);
        
        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHeaders(projectPage, "/api/projects/search/advanced"))
                .body(dtoPage);
    }
} 