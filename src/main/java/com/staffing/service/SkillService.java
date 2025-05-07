package com.staffing.service;

import com.staffing.dto.SkillDTO;
import java.util.List;

public interface SkillService {
    List<SkillDTO> getAllSkills();
    SkillDTO getSkillById(Long id);
    List<SkillDTO> getSkillsByCategory(String category);
    List<String> getAllCategories();
    SkillDTO createSkill(SkillDTO skillDTO);
    SkillDTO updateSkill(Long id, SkillDTO skillDTO);
    void deleteSkill(Long id);
    List<SkillDTO> searchSkills(String query);
} 