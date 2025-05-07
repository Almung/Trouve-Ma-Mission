package com.staffing.controller;

import com.staffing.dto.CollaboratorDTO;
import com.staffing.service.CollaboratorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/collaborators")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CollaboratorController {

    @Autowired
    private CollaboratorService collaboratorService;

    @GetMapping
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getAllCollaborators() {
        return ResponseEntity.ok(collaboratorService.getAllCollaborators());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<CollaboratorDTO> getCollaboratorById(@PathVariable Long id) {
        return ResponseEntity.ok(collaboratorService.getCollaboratorById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> searchCollaborators(@RequestParam String query) {
        return ResponseEntity.ok(collaboratorService.searchCollaborators(query));
    }

    @GetMapping("/available")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getAvailableCollaborators(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(collaboratorService.getAvailableCollaborators(date));
    }

    @GetMapping("/available/period")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getAvailableCollaboratorsForPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(collaboratorService.getAvailableCollaboratorsForPeriod(startDate, endDate));
    }

    @GetMapping("/skill")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getCollaboratorsBySkill(
            @RequestParam String skillName,
            @RequestParam(required = false, defaultValue = "0") Integer minLevel) {
        return ResponseEntity.ok(collaboratorService.getCollaboratorsBySkill(skillName, minLevel));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getCollaboratorsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(collaboratorService.getCollaboratorsByProject(projectId));
    }

    @PostMapping
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<CollaboratorDTO> createCollaborator(@Valid @RequestBody CollaboratorDTO collaboratorDTO) {
        return ResponseEntity.ok(collaboratorService.createCollaborator(collaboratorDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<CollaboratorDTO> updateCollaborator(
            @PathVariable Long id,
            @Valid @RequestBody CollaboratorDTO collaboratorDTO) {
        return ResponseEntity.ok(collaboratorService.updateCollaborator(id, collaboratorDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<Void> deleteCollaborator(@PathVariable Long id) {
        collaboratorService.deleteCollaborator(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorDTO> deactivateCollaborator(@PathVariable Long id) {
        return ResponseEntity.ok(collaboratorService.deactivateCollaborator(id));
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollaboratorDTO> reactivateCollaborator(@PathVariable Long id) {
        return ResponseEntity.ok(collaboratorService.reactivateCollaborator(id));
    }

    @GetMapping("/all")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getAllCollaboratorsIncludingInactive() {
        return ResponseEntity.ok(collaboratorService.getAllCollaborators());
    }

    @GetMapping("/active")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getActiveCollaborators() {
        return ResponseEntity.ok(collaboratorService.getActiveCollaborators());
    }

    @GetMapping("/inactive")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<CollaboratorDTO>> getInactiveCollaborators() {
        return ResponseEntity.ok(collaboratorService.getInactiveCollaborators());
    }

    @GetMapping("/statistics")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<Map<String, Object>> getCollaboratorStatistics() {
        System.out.println("Récupération des statistiques des collaborateurs...");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Statistiques de base
        statistics.put("total", collaboratorService.getTotalCollaborators());
        statistics.put("onMission", collaboratorService.getCollaboratorsOnMission());
        statistics.put("free", collaboratorService.getFreeCollaborators());
        statistics.put("onLeave", collaboratorService.getCollaboratorsOnLeave().size());
        
        // Récupérer toutes les statistiques en une fois
        Map<String, Object> allStats = collaboratorService.getCollaboratorStatistics();
        statistics.putAll(allStats);
        
        return ResponseEntity.ok(statistics);
    }
} 