package com.staffing.controller;

import com.staffing.model.ProjectAlert;
import com.staffing.service.ProjectAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectAlertController {
    private final ProjectAlertService alertService;

    public ProjectAlertController(ProjectAlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectAlert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/high-priority")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectAlert>> getHighPriorityAlerts() {
        return ResponseEntity.ok(alertService.getHighPriorityAlerts());
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<ProjectAlert>> getProjectAlerts(@PathVariable Long projectId) {
        return ResponseEntity.ok(alertService.getProjectAlerts(projectId));
    }

    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<ProjectAlert> resolveAlert(@PathVariable Long alertId) {
        return ResponseEntity.ok(alertService.resolveAlert(alertId));
    }
} 