package com.staffing.repository;

import com.staffing.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
    
    List<Notification> findByRecipientAndReadOrderByCreatedAtDesc(String recipient, boolean read);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.read = false")
    long countUnreadNotifications(@Param("recipient") String recipient);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient = :recipient AND n.read = false")
    void markAllAsRead(@Param("recipient") String recipient);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(String recipient, String type);
    
    List<Notification> findByRecipientAndPriorityOrderByCreatedAtDesc(String recipient, String priority);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(
        @Param("recipient") String recipient,
        @Param("since") LocalDateTime since
    );
    
    @Query("DELETE FROM Notification n WHERE n.recipient = :recipient AND n.read = true AND n.createdAt < :before")
    void deleteOldReadNotifications(
        @Param("recipient") String recipient,
        @Param("before") LocalDateTime before
    );
} 