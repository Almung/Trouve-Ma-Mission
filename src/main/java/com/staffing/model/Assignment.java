package com.staffing.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.staffing.model.enums.CollaboratorStatus;
import com.staffing.model.enums.ProjectStatus;
import com.staffing.model.enums.AssignmentRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "collaborator_id", nullable = false)
    private Collaborator collaborator;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String role;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void onSaveOrUpdate() {
        // Initialisation de createdAt lors de la création
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 