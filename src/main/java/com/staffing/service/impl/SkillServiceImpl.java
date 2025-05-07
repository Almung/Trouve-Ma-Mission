package com.staffing.service.impl;

import com.staffing.dto.SkillDTO;
import com.staffing.model.Skill;
import com.staffing.repository.SkillRepository;
import com.staffing.service.SkillService;
import com.staffing.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    @Autowired
    private SkillRepository skillRepository;

    @Override
    public List<SkillDTO> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SkillDTO getSkillById(Long id) {
        return skillRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
    }

    @Override
    public List<SkillDTO> getSkillsByCategory(String category) {
        return skillRepository.findByCategory(category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return skillRepository.findDistinctCategories();
    }

    @Override
    public SkillDTO createSkill(SkillDTO skillDTO) {
        Skill skill = convertToEntity(skillDTO);
        Skill savedSkill = skillRepository.save(skill);
        return convertToDTO(savedSkill);
    }

    @Override
    public SkillDTO updateSkill(Long id, SkillDTO skillDTO) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        
        skill.setName(skillDTO.getName());
        skill.setCategory(skillDTO.getCategory());
        
        Skill updatedSkill = skillRepository.save(skill);
        return convertToDTO(updatedSkill);
    }

    @Override
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill", "id", id);
        }
        skillRepository.deleteById(id);
    }

    @Override
    public List<SkillDTO> searchSkills(String query) {
        return skillRepository.searchSkills(query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SkillDTO convertToDTO(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        dto.setCategory(skill.getCategory());
        return dto;
    }

    private Skill convertToEntity(SkillDTO dto) {
        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        return skill;
    }
} 