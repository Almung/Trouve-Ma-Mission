package com.staffing.model.converter;

import com.staffing.model.enums.ProjectStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProjectStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ProjectStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return ProjectStatus.EN_DEMARRAGE; // Valeur par défaut
        }
    }
} 