package com.staffing.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CollaboratorStatus {
    DISPONIBLE("DISPONIBLE"),
    EN_MISSION("EN_MISSION"),
    EN_CONGE("EN_CONGE");

    private final String value;

    CollaboratorStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getLabel() {
        switch (this) {
            case DISPONIBLE:
                return "Disponible";
            case EN_MISSION:
                return "En mission";
            case EN_CONGE:
                return "En congé";
            default:
                return value;
        }
    }

    public static CollaboratorStatus fromValue(String value) {
        for (CollaboratorStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
} 