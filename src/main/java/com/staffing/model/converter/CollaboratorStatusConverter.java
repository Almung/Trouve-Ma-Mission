package com.staffing.model.converter;

import com.staffing.model.enums.CollaboratorStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CollaboratorStatusConverter implements AttributeConverter<CollaboratorStatus, String> {

    @Override
    public String convertToDatabaseColumn(CollaboratorStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public CollaboratorStatus convertToEntityAttribute(String value) {
        return value == null ? null : CollaboratorStatus.fromValue(value);
    }
} 