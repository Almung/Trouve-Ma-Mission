package com.staffing.model.enums;

public enum UserRole {
    ADMIN("Administrateur", true, true, true, true, true),
    MANAGER("Manager", true, true, false, true, false),
    USER("Utilisateur", true, false, false, false, false);

    private final String label;
    private final boolean canRead;
    private final boolean canWrite;
    private final boolean canManageUsers;
    private final boolean canManageAssignments;
    private final boolean canDelete;

    UserRole(String label, boolean canRead, boolean canWrite, boolean canManageUsers, boolean canManageAssignments, boolean canDelete) {
        this.label = label;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canManageUsers = canManageUsers;
        this.canManageAssignments = canManageAssignments;
        this.canDelete = canDelete;
    }

    public String getLabel() {
        return label;
    }

    public boolean canRead() {
        return canRead;
    }

    public boolean canWrite() {
        return canWrite;
    }

    public boolean canManageUsers() {
        return canManageUsers;
    }

    public boolean canManageAssignments() {
        return canManageAssignments;
    }

    public boolean canDelete() {
        return canDelete;
    }
} 