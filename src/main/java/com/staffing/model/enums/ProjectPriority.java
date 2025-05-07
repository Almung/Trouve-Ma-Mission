package com.staffing.model.enums;

public enum ProjectPriority {
    CRITIQUE("Critique"),
    HAUTE("Haute"),
    MOYENNE("Moyenne"),
    BASSE("Basse");

    private final String label;

    ProjectPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
} 