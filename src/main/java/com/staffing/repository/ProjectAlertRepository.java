package com.staffing.repository;

import com.staffing.model.ProjectAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectAlertRepository extends JpaRepository<ProjectAlert, Long> {
    List<ProjectAlert> findByProjectIdAndIsResolvedFalse(Long projectId);
    
    List<ProjectAlert> findByIsResolvedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM ProjectAlert a WHERE a.isResolved = false AND a.severity IN ('HIGH', 'CRITICAL') ORDER BY a.createdAt DESC")
    List<ProjectAlert> findHighPriorityAlerts();
    
    @Query("SELECT a FROM ProjectAlert a WHERE a.project.id = :projectId AND a.createdAt >= :since AND a.isResolved = false")
    List<ProjectAlert> findRecentAlertsByProject(@Param("projectId") Long projectId, @Param("since") LocalDateTime since);
    
    List<ProjectAlert> findByTypeAndIsResolvedFalse(ProjectAlert.AlertType type);
} 