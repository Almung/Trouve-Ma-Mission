package com.staffing.dto;

import com.staffing.model.Collaborator;
import com.staffing.model.Skill;
import com.staffing.model.enums.CollaboratorStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CollaboratorDTO {
    private Long id;

    @NotBlank(message = "Le nom est requis")
    private String name;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le rôle est requis")
    private String role;

    private String grade;

    @NotBlank(message = "Le numéro de téléphone est requis")
    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$", message = "Le format du numéro de téléphone est invalide")
    private String phone;

    @NotNull(message = "Le statut est requis")
    private CollaboratorStatus status;

    @NotNull(message = "Les années d'expérience sont requises")
    private Integer experienceYears;

    private Set<String> skillNames;
    private Set<Long> skillIds;
    private LocalDate dateEmbauche;
    private String photoUrl;
    private String linkedin;
    private String github;
    private String cv;
    private String commentaire;
    private Double tauxJournalier;
    private String localisation;
    private Boolean disponible;

    // Champs enrichis pour l'affichage
    private Set<SkillDTO> skills;
    private Set<AssignmentDTO> assignments;
    private boolean active;

    public static CollaboratorDTO fromEntity(Collaborator collaborator) {
        CollaboratorDTO dto = new CollaboratorDTO();
        dto.setId(collaborator.getId());
        dto.setName(collaborator.getName());
        dto.setEmail(collaborator.getEmail());
        dto.setRole(collaborator.getRole());
        dto.setGrade(collaborator.getGrade());
        dto.setPhone(collaborator.getPhone());
        dto.setStatus(collaborator.getStatus());
        dto.setExperienceYears(collaborator.getExperienceYears());
        dto.setActive(collaborator.isActive());
        
        if (collaborator.getSkills() != null) {
            dto.setSkills(collaborator.getSkills().stream()
                    .map(SkillDTO::fromEntity)
                    .collect(Collectors.toSet()));
            
            // Extraire les noms des compétences
            dto.setSkillNames(collaborator.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }

    public Collaborator toEntity() {
        Collaborator collaborator = new Collaborator();
        collaborator.setId(this.id);
        collaborator.setName(this.name);
        collaborator.setEmail(this.email);
        collaborator.setRole(this.role);
        collaborator.setGrade(this.grade);
        collaborator.setPhone(this.phone);
        collaborator.setStatus(this.status);
        collaborator.setExperienceYears(this.experienceYears);

        // Les compétences sont gérées dans le service
        return collaborator;
    }
} 