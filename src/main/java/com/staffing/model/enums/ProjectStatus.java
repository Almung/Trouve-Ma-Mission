package com.staffing.model.enums;

public enum ProjectStatus {
    EN_DEMARRAGE("En démarrage"),
    EN_COURS("En cours"),
    EN_PAUSE("En pause"),
    TERMINE("Terminé"),
    ANNULE("Annulé");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
} 