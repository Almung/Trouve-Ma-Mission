package com.staffing.repository;

import com.staffing.model.Collaborator;
import com.staffing.model.enums.CollaboratorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    List<Collaborator> findByStatus(CollaboratorStatus status);
    
    List<Collaborator> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    
    @Query("SELECT c FROM Collaborator c WHERE c.status = :status AND NOT EXISTS " +
           "(SELECT a FROM Assignment a WHERE a.collaborator = c AND :date BETWEEN a.project.startDate AND a.project.endDate)")
    List<Collaborator> findAvailableCollaboratorsForDate(@Param("status") CollaboratorStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT DISTINCT c FROM Collaborator c " +
           "JOIN c.skills s " +
           "WHERE s.name = :skillName")
    List<Collaborator> findBySkill(@Param("skillName") String skillName);
    
    @Query("SELECT c FROM Collaborator c WHERE c.status = :status AND c.role = :role")
    List<Collaborator> findByStatusAndRole(@Param("status") CollaboratorStatus status, @Param("role") String role);
    
    @Query("SELECT DISTINCT c FROM Collaborator c JOIN c.assignments a WHERE a.project.id = :projectId")
    List<Collaborator> findByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT c FROM Collaborator c WHERE NOT EXISTS " +
           "(SELECT a FROM Assignment a WHERE a.collaborator = c AND :startDate <= a.project.endDate AND :endDate >= a.project.startDate)")
    List<Collaborator> findAvailableForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE c.id = :id")
    Optional<Collaborator> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills")
    List<Collaborator> findAllWithSkills();

    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE c.status = :status")
    List<Collaborator> findByStatusWithSkills(@Param("status") CollaboratorStatus status);
    
    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE c.name LIKE %:query% OR c.email LIKE %:query%")
    List<Collaborator> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseWithSkills(@Param("query") String query);
    
    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE c.status = :status AND NOT EXISTS " +
           "(SELECT a FROM Assignment a WHERE a.collaborator = c AND :date BETWEEN a.project.startDate AND a.project.endDate)")
    List<Collaborator> findAvailableCollaboratorsForDateWithSkills(@Param("status") CollaboratorStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT DISTINCT c FROM Collaborator c LEFT JOIN FETCH c.skills " +
           "JOIN c.skills s WHERE s.name = :skillName")
    List<Collaborator> findBySkillWithSkills(@Param("skillName") String skillName);
    
    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE c.status = :status AND c.role = :role")
    List<Collaborator> findByStatusAndRoleWithSkills(@Param("status") CollaboratorStatus status, @Param("role") String role);
    
    @Query("SELECT DISTINCT c FROM Collaborator c LEFT JOIN FETCH c.skills JOIN c.assignments a WHERE a.project.id = :projectId")
    List<Collaborator> findByProjectIdWithSkills(@Param("projectId") Long projectId);
    
    @Query("SELECT c FROM Collaborator c LEFT JOIN FETCH c.skills WHERE NOT EXISTS " +
           "(SELECT a FROM Assignment a WHERE a.collaborator = c AND :startDate <= a.project.endDate AND :endDate >= a.project.startDate)")
    List<Collaborator> findAvailableForPeriodWithSkills(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Recherche des collaborateurs actifs
    @Query("SELECT c FROM Collaborator c WHERE c.active = true")
    List<Collaborator> findActiveCollaborators();

    // Recherche des collaborateurs inactifs
    @Query("SELECT c FROM Collaborator c WHERE c.active = false")
    List<Collaborator> findInactiveCollaborators();

    // Recherche des collaborateurs actifs par statut
    @Query("SELECT c FROM Collaborator c WHERE c.active = true AND c.status = :status")
    List<Collaborator> findActiveCollaboratorsByStatus(@Param("status") CollaboratorStatus status);

    // Recherche des collaborateurs actifs disponibles
    @Query("SELECT c FROM Collaborator c WHERE c.active = true AND c.status = 'DISPONIBLE'")
    List<Collaborator> findActiveAvailableCollaborators();

    // Compter les collaborateurs par statut
    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.status = :status")
    long countByStatus(@Param("status") CollaboratorStatus status);

    @Query("SELECT s.name, COUNT(c) FROM Collaborator c JOIN c.skills s GROUP BY s.name")
    List<Object[]> countCollaboratorsBySkill();

    @Query("SELECT s.name, COUNT(c) as count FROM Collaborator c JOIN c.skills s GROUP BY s.name ORDER BY count DESC")
    List<Object[]> findTopSkills(@Param("limit") int limit);

    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.active = :active")
    long countByActive(@Param("active") boolean active);

    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.status = :status AND c.active = :active")
    long countByStatusAndActive(@Param("status") CollaboratorStatus status, @Param("active") boolean active);

    List<Collaborator> findByActive(boolean active);
} 