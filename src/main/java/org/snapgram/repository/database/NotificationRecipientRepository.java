package org.snapgram.repository.database;

import org.snapgram.entity.database.notification.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, UUID> {
    @Modifying
    @Query("UPDATE NotificationRecipient SET isRead = true WHERE recipient.id = :recipientId AND isRead = false")
    void markAsRead(@Param("recipientId") UUID recipientId);

    @Modifying
    void deleteByNotificationEntity_Id(UUID notificationId);
}
