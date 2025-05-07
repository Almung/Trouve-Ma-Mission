package com.staffing.service;

import com.staffing.model.Notification;
import com.staffing.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(username);
    }

    public List<Notification> getUnreadNotifications(String username) {
        return notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(username, false);
    }

    public long getUnreadCount(String username) {
        return notificationRepository.countUnreadNotifications(username);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsRead(username);
    }

    public List<Notification> getNotificationsByType(String username, String type) {
        return notificationRepository.findByRecipientAndTypeOrderByCreatedAtDesc(username, type);
    }

    public List<Notification> getNotificationsByPriority(String username, String priority) {
        return notificationRepository.findByRecipientAndPriorityOrderByCreatedAtDesc(username, priority);
    }

    public List<Notification> getRecentNotifications(String username, LocalDateTime since) {
        return notificationRepository.findRecentNotifications(username, since);
    }

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldReadNotifications("system", thirtyDaysAgo);
    }

    // Méthodes utilitaires pour créer des notifications spécifiques
    public Notification createProjectNotification(String username, String projectName, String action) {
        Notification notification = new Notification();
        notification.setType("PROJECT");
        notification.setTitle("Project " + action);
        notification.setMessage("Project '" + projectName + "' has been " + action);
        notification.setRecipient(username);
        notification.setPriority("MEDIUM");
        notification.setLink("/projects/" + projectName);
        return createNotification(notification);
    }

    public Notification createAssignmentNotification(String username, String collaboratorName, String projectName) {
        Notification notification = new Notification();
        notification.setType("ASSIGNMENT");
        notification.setTitle("New Assignment");
        notification.setMessage(collaboratorName + " has been assigned to " + projectName);
        notification.setRecipient(username);
        notification.setLink("/assignments/recent");
        return notificationRepository.save(notification);
    }

    public Notification createSystemNotification(String username, String title, String message, String priority) {
        Notification notification = new Notification();
        notification.setType("SYSTEM");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(username);
        notification.setPriority(priority);
        return notificationRepository.save(notification);
    }
} 