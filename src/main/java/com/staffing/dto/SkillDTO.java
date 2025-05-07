package com.staffing.dto;

import com.staffing.model.Skill;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
public class SkillDTO {
    private Long id;

    @NotBlank(message = "Le nom de la compétence est requis")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String name;

    @NotBlank(message = "La catégorie est requise")
    @Size(min = 2, max = 50, message = "La catégorie doit contenir entre 2 et 50 caractères")
    private String category;

    // Statistiques
    private Integer projectCount;
    private Integer collaboratorCount;

    public SkillDTO(String name) {
        this.name = name;
        this.category = "Général"; // Catégorie par défaut
    }

    public static SkillDTO fromEntity(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        
        // Calcul des statistiques
        if (skill.getCollaborators() != null) {
            dto.setCollaboratorCount(skill.getCollaborators().size());
        }
        if (skill.getProjects() != null) {
            dto.setProjectCount(skill.getProjects().size());
        }
        
        return dto;
    }

    public Skill toEntity() {
        Skill skill = new Skill();
        skill.setId(this.id);
        skill.setName(this.name);
        skill.setCategory(this.category);
        return skill;
    }
} 