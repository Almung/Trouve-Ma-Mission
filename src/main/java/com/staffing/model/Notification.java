package com.staffing.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // PROJECT, COLLABORATOR, ASSIGNMENT, SYSTEM

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String recipient; // username of the recipient

    @Column
    private String link; // URL to the related resource

    @Column(nullable = false)
    private String priority; // HIGH, MEDIUM, LOW

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 