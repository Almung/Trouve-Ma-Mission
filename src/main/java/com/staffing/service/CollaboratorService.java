package com.staffing.service;

import com.staffing.dto.CollaboratorDTO;
import com.staffing.model.Collaborator;
import com.staffing.model.Assignment;
import com.staffing.model.Skill;
import com.staffing.model.enums.CollaboratorStatus;
import com.staffing.repository.CollaboratorRepository;
import com.staffing.repository.AssignmentRepository;
import com.staffing.repository.SkillRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.staffing.exception.CollaboratorStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaboratorService {
    private final CollaboratorRepository collaboratorRepository;
    private final AssignmentRepository assignmentRepository;
    private final SkillRepository skillRepository;

    public List<CollaboratorDTO> getAllCollaborators() {
        return collaboratorRepository.findAll().stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CollaboratorDTO getCollaboratorById(Long id) {
        return collaboratorRepository.findByIdWithSkills(id)
                .map(CollaboratorDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));
    }

    public List<CollaboratorDTO> getAvailableCollaborators(LocalDate date) {
        return collaboratorRepository.findAvailableCollaboratorsForDateWithSkills(CollaboratorStatus.DISPONIBLE, date).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CollaboratorDTO> searchCollaborators(String query) {
        return collaboratorRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseWithSkills(query).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CollaboratorDTO createCollaborator(CollaboratorDTO collaboratorDTO) {
        System.out.println("Creating collaborator with skills: " + collaboratorDTO.getSkillNames());
        Collaborator collaborator = collaboratorDTO.toEntity();
        
        // Gérer les compétences
        if (collaboratorDTO.getSkillNames() != null && !collaboratorDTO.getSkillNames().isEmpty()) {
            Set<Skill> skills = new HashSet<>();
            for (String skillName : collaboratorDTO.getSkillNames()) {
                System.out.println("Processing skill: " + skillName);
                Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                        .orElseGet(() -> {
                            System.out.println("Creating new skill: " + skillName);
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            newSkill.setCategory("Général"); // Catégorie par défaut
                            return skillRepository.save(newSkill);
                        });
                skills.add(skill);
            }
            collaborator.setSkills(skills);
            System.out.println("Set skills to collaborator: " + skills);
        }

        collaborator = collaboratorRepository.save(collaborator);
        System.out.println("Saved collaborator with ID: " + collaborator.getId());
        
        // Récupérer le collaborateur avec ses compétences après la sauvegarde
        Collaborator savedCollaborator = collaboratorRepository.findByIdWithSkills(collaborator.getId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found after creation"));
        System.out.println("Retrieved saved collaborator with skills: " + savedCollaborator.getSkills());
        
        return CollaboratorDTO.fromEntity(savedCollaborator);
    }

    private boolean hasActiveAssignments(Long collaboratorId) {
        return assignmentRepository.existsByCollaboratorIdAndProjectActiveTrue(collaboratorId);
    }

    public CollaboratorDTO updateCollaborator(Long id, CollaboratorDTO collaboratorDTO) {
        Collaborator existingCollaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));

        // Vérifier si le collaborateur a des affectations actives
        if (hasActiveAssignments(id) && 
            existingCollaborator.getStatus() != collaboratorDTO.getStatus()) {
            throw new CollaboratorStatusException(
                "Impossible de modifier le statut du collaborateur " + existingCollaborator.getName() + 
                " car il est actuellement affecté à un ou plusieurs projets actifs. " +
                "Veuillez d'abord terminer ou annuler ses affectations avant de changer son statut."
            );
        }

        // Mettre à jour les champs autorisés
        existingCollaborator.setName(collaboratorDTO.getName());
        existingCollaborator.setEmail(collaboratorDTO.getEmail());
        existingCollaborator.setPhone(collaboratorDTO.getPhone());
        existingCollaborator.setRole(collaboratorDTO.getRole());
        existingCollaborator.setGrade(collaboratorDTO.getGrade());
        existingCollaborator.setExperienceYears(collaboratorDTO.getExperienceYears());
        existingCollaborator.setActive(collaboratorDTO.isActive());

        // Mettre à jour le statut seulement si le collaborateur n'a pas d'affectations actives
        if (!hasActiveAssignments(id)) {
            existingCollaborator.setStatus(collaboratorDTO.getStatus());
        }

        // Mettre à jour les compétences
        Set<Skill> skills = new HashSet<>();
        for (String skillName : collaboratorDTO.getSkillNames()) {
            Skill skill = skillRepository.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(skillName);
                        newSkill.setCategory("Général");
                        return skillRepository.save(newSkill);
                    });
            skills.add(skill);
        }
        existingCollaborator.setSkills(skills);

        return CollaboratorDTO.fromEntity(collaboratorRepository.save(existingCollaborator));
    }

    public void deleteCollaborator(Long id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));

        // Vérifier s'il y a des affectations actives
        List<Assignment> activeAssignments = assignmentRepository.findByCollaboratorId(id);
        if (!activeAssignments.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer un collaborateur ayant des affectations actives");
        }

        collaboratorRepository.delete(collaborator);
    }

    public List<CollaboratorDTO> getCollaboratorsBySkill(String skillName, Integer minLevel) {
        return collaboratorRepository.findBySkillWithSkills(skillName).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CollaboratorDTO> getCollaboratorsByProject(Long projectId) {
        return collaboratorRepository.findByProjectIdWithSkills(projectId).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CollaboratorDTO> getAvailableCollaboratorsForPeriod(LocalDate startDate, LocalDate endDate) {
        return collaboratorRepository.findAvailableForPeriodWithSkills(startDate, endDate).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Désactiver un collaborateur
    public CollaboratorDTO deactivateCollaborator(Long id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));

        // Vérifier si le collaborateur a des affectations actives
        List<Assignment> activeAssignments = assignmentRepository.findByCollaboratorId(id);
        if (!activeAssignments.isEmpty()) {
            throw new IllegalStateException("Impossible de désactiver un collaborateur ayant des affectations actives");
        }

        collaborator.setActive(false);
        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        return CollaboratorDTO.fromEntity(savedCollaborator);
    }

    // Réactiver un collaborateur
    public CollaboratorDTO reactivateCollaborator(Long id) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));
        
        collaborator.setActive(true);
        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);
        return CollaboratorDTO.fromEntity(savedCollaborator);
    }

    // Obtenir les collaborateurs actifs
    public List<CollaboratorDTO> getActiveCollaborators() {
        return collaboratorRepository.findActiveCollaborators().stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les collaborateurs inactifs
    public List<CollaboratorDTO> getInactiveCollaborators() {
        return collaboratorRepository.findInactiveCollaborators().stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les collaborateurs actifs par statut
    public List<CollaboratorDTO> getActiveCollaboratorsByStatus(CollaboratorStatus status) {
        return collaboratorRepository.findActiveCollaboratorsByStatus(status).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir les collaborateurs actifs disponibles
    public List<CollaboratorDTO> getActiveAvailableCollaborators() {
        return collaboratorRepository.findActiveAvailableCollaborators().stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Désactiver plusieurs collaborateurs en masse
    public void deactivateCollaborators(List<Long> collaboratorIds) {
        for (Long id : collaboratorIds) {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));
            
            // Vérifier s'il y a des affectations actives
            List<Assignment> activeAssignments = assignmentRepository.findByCollaboratorId(id);
            if (!activeAssignments.isEmpty()) {
                throw new IllegalStateException("Impossible de désactiver le collaborateur " + 
                    collaborator.getName() + 
                    " car il a des affectations actives");
            }
            
            collaborator.setActive(false);
            collaboratorRepository.save(collaborator);
        }
    }

    // Réactiver plusieurs collaborateurs en masse
    public void reactivateCollaborators(List<Long> collaboratorIds) {
        for (Long id : collaboratorIds) {
            Collaborator collaborator = collaboratorRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Collaborator not found with id: " + id));
            
            collaborator.setActive(true);
            collaboratorRepository.save(collaborator);
        }
    }

    // Obtenir les statistiques des collaborateurs actifs/inactifs
    public Map<String, Long> getCollaboratorStatusStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", collaboratorRepository.count());
        statistics.put("active", collaboratorRepository.countByActive(true));
        statistics.put("inactive", collaboratorRepository.countByActive(false));
        return statistics;
    }

    // Obtenir le nombre de collaborateurs en mission
    public long getCollaboratorsOnMission() {
        return collaboratorRepository.countByStatus(CollaboratorStatus.EN_MISSION);
    }

    // Obtenir le nombre de collaborateurs libres
    public long getFreeCollaborators() {
        return collaboratorRepository.countByStatus(CollaboratorStatus.DISPONIBLE);
    }

    // Obtenir les collaborateurs en congé
    public List<CollaboratorDTO> getCollaboratorsOnLeave() {
        return collaboratorRepository.findByStatus(CollaboratorStatus.EN_CONGE).stream()
                .map(CollaboratorDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Obtenir le nombre total de collaborateurs
    public Long getTotalCollaborators() {
        return collaboratorRepository.count();
    }

    // Obtenir les statistiques des collaborateurs par compétences
    public Map<String, Object> getCollaboratorStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Statistiques de base - ne compter que les collaborateurs actifs
        long totalActive = collaboratorRepository.countByActive(true);
        long onMission = collaboratorRepository.countByStatusAndActive(CollaboratorStatus.EN_MISSION, true);
        long free = collaboratorRepository.countByStatusAndActive(CollaboratorStatus.DISPONIBLE, true);
        long onLeave = collaboratorRepository.countByStatusAndActive(CollaboratorStatus.EN_CONGE, true);
        
        // Vérification de la cohérence des données
        long sumStatus = onMission + free + onLeave;
        if (sumStatus != totalActive) {
            System.out.println("ATTENTION: La somme des statuts (" + sumStatus + ") ne correspond pas au total des collaborateurs actifs (" + totalActive + ")");
        }
        
        statistics.put("total", totalActive);
        statistics.put("onMission", onMission);
        statistics.put("free", free);
        statistics.put("onLeave", onLeave);
        
        // Statistiques des compétences
        Map<String, Long> skillStats = calculateSkillStatistics();
        
        // Top 3 compétences
        List<Map<String, Object>> topSkills = skillStats.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .limit(3)
            .map(entry -> {
                Map<String, Object> skillData = new HashMap<>();
                skillData.put("name", entry.getKey());
                skillData.put("count", entry.getValue());
                return skillData;
            })
            .collect(Collectors.toList());
        
        // Compétences les moins utilisées (moins de 3 utilisations)
        List<Map<String, Object>> leastUsedSkills = skillStats.entrySet().stream()
            .filter(entry -> entry.getValue() < 3)
            .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .map(entry -> {
                Map<String, Object> skillData = new HashMap<>();
                skillData.put("name", entry.getKey());
                skillData.put("count", entry.getValue());
                return skillData;
            })
            .collect(Collectors.toList());
        
        statistics.put("skills", skillStats);
        statistics.put("topSkills", topSkills);
        statistics.put("leastUsedSkills", leastUsedSkills);
        
        // Logs détaillés
        System.out.println("Statistiques des compétences calculées:");
        System.out.println("Nombre total de compétences uniques: " + skillStats.size());
        System.out.println("Top 3 compétences: " + topSkills);
        System.out.println("Compétences sous-utilisées: " + leastUsedSkills);
        
        return statistics;
    }

    private Map<String, Long> calculateSkillStatistics() {
        Map<String, Long> skillStats = new HashMap<>();
        List<Collaborator> collaborators = collaboratorRepository.findByActive(true);
        
        System.out.println("Calcul des statistiques des compétences pour " + collaborators.size() + " collaborateurs actifs");
        
        for (Collaborator collaborator : collaborators) {
            Set<Skill> skills = collaborator.getSkills();
            if (skills == null || skills.isEmpty()) {
                System.out.println("ATTENTION: Le collaborateur " + collaborator.getName() + " n'a pas de compétences");
                continue;
            }
            
            for (Skill skill : skills) {
                String skillName = skill.getName();
                if (skillName == null || skillName.trim().isEmpty()) {
                    System.out.println("ATTENTION: Compétence sans nom trouvée pour le collaborateur " + collaborator.getName());
                    continue;
                }
                
                skillStats.put(skillName, skillStats.getOrDefault(skillName, 0L) + 1);
            }
        }
        
        return skillStats;
    }

    public List<Map<String, Object>> getTopSkills() {
        List<Object[]> results = collaboratorRepository.findTopSkills(3);
        return results.stream()
                .map(result -> {
                    Map<String, Object> skillData = new HashMap<>();
                    skillData.put("name", result[0]);
                    skillData.put("count", result[1]);
                    return skillData;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getLeastUsedSkills() {
        Map<String, Long> skillStats = new HashMap<>();
        List<Collaborator> collaborators = collaboratorRepository.findByActive(true);
        
        // Compter l'utilisation de chaque compétence
        for (Collaborator collaborator : collaborators) {
            for (Skill skill : collaborator.getSkills()) {
                skillStats.put(skill.getName(), skillStats.getOrDefault(skill.getName(), 0L) + 1);
            }
        }
        
        // Trier par utilisation croissante et prendre les 5 premières
        return skillStats.entrySet().stream()
            .sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
            .limit(5)
            .map(entry -> {
                Map<String, Object> skillData = new HashMap<>();
                skillData.put("name", entry.getKey());
                skillData.put("count", entry.getValue());
                return skillData;
            })
            .collect(Collectors.toList());
    }
} 