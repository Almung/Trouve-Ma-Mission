package com.staffing.repository;

import com.staffing.model.Assignment;
import com.staffing.model.Collaborator;
import com.staffing.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCollaboratorId(Long collaboratorId);
    List<Assignment> findByProjectId(Long projectId);
    Optional<Assignment> findByCollaborator(Collaborator collaborator);
    boolean existsByCollaborator(Collaborator collaborator);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.createdAt >= :date")
    int countRecentAssignments(@Param("date") LocalDateTime date);

    // Trouver une affectation par projet et collaborateur
    @Query("SELECT a FROM Assignment a WHERE a.project.id = :projectId AND a.collaborator.id = :collaboratorId")
    Optional<Assignment> findByProjectIdAndCollaboratorId(@Param("projectId") Long projectId, @Param("collaboratorId") Long collaboratorId);

    // Trouver les affectations actives d'un collaborateur
    @Query("SELECT a FROM Assignment a WHERE a.collaborator.id = :collaboratorId AND a.project.status IN ('EN_DEMARRAGE', 'EN_COURS', 'EN_PAUSE')")
    List<Assignment> findActiveAssignmentsByCollaboratorId(@Param("collaboratorId") Long collaboratorId);

    // Trouver les affectations actives d'un projet
    @Query("SELECT a FROM Assignment a WHERE a.project.id = :projectId AND a.project.status IN ('EN_DEMARRAGE', 'EN_COURS', 'EN_PAUSE')")
    List<Assignment> findActiveAssignmentsByProjectId(@Param("projectId") Long projectId);

    // Trouver les affectations qui se terminent bientôt
    @Query("SELECT a FROM Assignment a WHERE a.project.endDate <= :endDate AND a.project.status IN ('EN_DEMARRAGE', 'EN_COURS', 'EN_PAUSE')")
    List<Assignment> findAssignmentsEndingSoon(@Param("endDate") LocalDate endDate);

    // Trouver les affectations par période
    @Query("SELECT a FROM Assignment a WHERE a.project.startDate <= :endDate AND a.project.endDate >= :startDate")
    List<Assignment> findAssignmentsByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Assignment a WHERE a.collaborator.id = :collaboratorId AND a.project.status = :status")
    List<Assignment> findByCollaboratorIdAndProjectStatus(@Param("collaboratorId") Long collaboratorId, @Param("status") ProjectStatus status);

    @Query("SELECT a FROM Assignment a WHERE a.project.endDate < :date")
    List<Assignment> findByProjectEndDateBefore(@Param("date") LocalDate date);

    @Query("SELECT a FROM Assignment a WHERE a.project.id = :projectId AND a.collaborator.id IN :collaboratorIds")
    List<Assignment> findByProjectIdAndCollaboratorIdIn(@Param("projectId") Long projectId, @Param("collaboratorIds") List<Long> collaboratorIds);

    List<Assignment> findByProjectStatusIn(List<ProjectStatus> statuses);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.project.status = 'EN_COURS'")
    long countActiveAssignments();
    
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.project.endDate BETWEEN CURRENT_DATE AND :endDate")
    long countAssignmentsEndingSoon(@Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT a.collaborator) FROM Assignment a WHERE a.project.endDate BETWEEN CURRENT_DATE AND :endDate")
    long countCollaboratorsToBeReleased(@Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) > 0 FROM Assignment a WHERE a.collaborator.id = :collaboratorId AND a.project.active = true")
    boolean existsByCollaboratorIdAndProjectActiveTrue(@Param("collaboratorId") Long collaboratorId);
} 