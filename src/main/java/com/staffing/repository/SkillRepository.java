package com.staffing.repository;

import com.staffing.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
    
    List<Skill> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT DISTINCT s FROM Skill s JOIN s.collaborators c WHERE c.id = :collaboratorId")
    List<Skill> findSkillsByCollaboratorId(@Param("collaboratorId") Long collaboratorId);
    
    @Query("SELECT DISTINCT s FROM Skill s JOIN s.projects p WHERE p.id = :projectId")
    List<Skill> findSkillsByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT s FROM Skill s WHERE s.name IN :skillNames")
    List<Skill> findBySkillNames(@Param("skillNames") List<String> skillNames);
    
    boolean existsByNameIgnoreCase(String name);

    List<Skill> findByCategory(String category);

    @Query("SELECT DISTINCT s.category FROM Skill s")
    List<String> findDistinctCategories();

    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Skill> searchSkills(@Param("query") String query);
} 