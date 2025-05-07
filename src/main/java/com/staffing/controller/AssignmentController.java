package com.staffing.controller;

import com.staffing.dto.AssignmentDTO;
import com.staffing.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments() {
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @GetMapping("/collaborator/{collaboratorId}")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByCollaborator(@PathVariable Long collaboratorId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCollaboratorId(collaboratorId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignmentDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDTO> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentDTO assignmentDTO) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, assignmentDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/project/{projectId}/remove-collaborators")
    public ResponseEntity<Void> removeCollaboratorsFromProject(
            @PathVariable Long projectId,
            @RequestBody List<Long> collaboratorIds) {
        assignmentService.removeCollaboratorsFromProject(projectId, collaboratorIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/project/{projectId}/remove-all")
    public ResponseEntity<Void> removeAllCollaboratorsFromProject(@PathVariable Long projectId) {
        assignmentService.removeAllCollaboratorsFromProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/collaborator/{collaboratorId}/remove-all")
    public ResponseEntity<Void> removeCollaboratorFromAllProjects(@PathVariable Long collaboratorId) {
        assignmentService.removeCollaboratorFromAllProjects(collaboratorId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/remove-ending")
    public ResponseEntity<Void> removeCollaboratorsFromEndingProjects() {
        assignmentService.removeCollaboratorsFromEndingProjects();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/removal-stats")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<Map<String, Object>> getRemovalStatistics() {
        return ResponseEntity.ok(assignmentService.getRemovalStatistics());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AssignmentDTO>> getActiveAssignments() {
        return ResponseEntity.ok(assignmentService.getActiveAssignments());
    }
} 