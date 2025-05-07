package com.staffing.controller;

import com.staffing.dto.SkillDTO;
import com.staffing.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<SkillDTO>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<SkillDTO> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<SkillDTO>> getSkillsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(skillService.getSkillsByCategory(category));
    }

    @GetMapping("/categories")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(skillService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillDTO skillDTO) {
        return ResponseEntity.ok(skillService.createSkill(skillDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<SkillDTO> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO skillDTO) {
        return ResponseEntity.ok(skillService.updateSkill(id, skillDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authService.canWrite()")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("@authService.canRead()")
    public ResponseEntity<List<SkillDTO>> searchSkills(@RequestParam String query) {
        return ResponseEntity.ok(skillService.searchSkills(query));
    }
} 