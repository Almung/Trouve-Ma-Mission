package com.staffing.controller;

import com.staffing.model.Notification;
import com.staffing.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<Notification> getUserNotifications(Authentication authentication) {
        return notificationService.getUserNotifications(authentication.getName());
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(Authentication authentication) {
        return notificationService.getUnreadNotifications(authentication.getName());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/type/{type}")
    public List<Notification> getNotificationsByType(
            @PathVariable String type,
            Authentication authentication) {
        return notificationService.getNotificationsByType(authentication.getName(), type);
    }

    @GetMapping("/priority/{priority}")
    public List<Notification> getNotificationsByPriority(
            @PathVariable String priority,
            Authentication authentication) {
        return notificationService.getNotificationsByPriority(authentication.getName(), priority);
    }

    @GetMapping("/recent")
    public List<Notification> getRecentNotifications(
            @RequestParam(defaultValue = "24") int hours,
            Authentication authentication) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return notificationService.getRecentNotifications(authentication.getName(), since);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        // TODO: Implement delete functionality
        return ResponseEntity.ok().build();
    }
} 