package com.staffing.repository;

import com.staffing.model.Project;
import com.staffing.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    List<Project> findByStatus(ProjectStatus status);
    
    List<Project> findByClient(String client);
    
    List<Project> findByNameContainingIgnoreCaseOrClientContainingIgnoreCase(String name, String client);
    
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.endDate >= CURRENT_DATE")
    List<Project> findActiveProjectsByStatus(@Param("status") ProjectStatus status);
    
    @Query("SELECT p FROM Project p WHERE p.endDate >= CURRENT_DATE AND p.status = 'IN_PROGRESS'")
    List<Project> findInProgressProjects();
    
    @Query("SELECT p FROM Project p WHERE :skill MEMBER OF p.skills")
    List<Project> findBySkill(@Param("skill") String skill);
    
    @Query("SELECT DISTINCT p FROM Project p JOIN p.assignments a WHERE a.collaborator.id = :collaboratorId")
    List<Project> findByCollaboratorId(@Param("collaboratorId") Long collaboratorId);
    
    @Query("SELECT p FROM Project p WHERE p.endDate < :currentDate AND p.status != 'COMPLETED'")
    List<Project> findCriticalProjects(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT p FROM Project p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:startDate IS NULL OR p.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.endDate <= :endDate)")
    List<Project> findBySearchCriteria(
            @Param("name") String name,
            @Param("status") ProjectStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN p.skills s " +
           "WHERE s.name IN :skills")
    List<Project> findBySkills(@Param("skills") List<String> skills);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.endDate < CURRENT_DATE AND p.status = :status")
    long countByStatusAndOverdue(@Param("status") ProjectStatus status);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.updatedAt >= :date")
    int countRecentUpdates(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Project p WHERE p.active = true")
    List<Project> findByActiveTrue();

    // Recherche des projets actifs par statut
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.status = :status")
    List<Project> findActiveProjectsByStatusAndActive(@Param("status") ProjectStatus status);

    // Recherche des projets inactifs
    @Query("SELECT p FROM Project p WHERE p.active = false")
    List<Project> findInactiveProjects();

    // Recherche des projets actifs par client
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.client = :client")
    List<Project> findActiveProjectsByClient(@Param("client") String client);

    // Recherche des projets actifs en cours
    @Query("SELECT p FROM Project p WHERE p.active = true AND p.status = 'EN_COURS'")
    List<Project> findActiveInProgressProjects();

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.skills WHERE p.id = :id")
    Optional<Project> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.endDate < CURRENT_DATE")
    long countOverdueProjects(@Param("status") ProjectStatus status);
} 