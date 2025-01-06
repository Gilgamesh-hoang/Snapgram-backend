package org.snapgram.repository.database;

import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.entity.database.notification.NotificationEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationEntityRepository extends JpaRepository<NotificationEntity, UUID> {
    @Query("SELECT new org.snapgram.dto.NotificationResultDTO(e, r.isRead, t.actor.id, r.recipient.id) FROM NotificationEntity e " +
            "INNER JOIN NotificationRecipient r ON r.notificationEntity.id = e.id " +
            "INNER JOIN NotificationTrigger t ON t.notificationEntity.id = e.id " +
            "WHERE r.recipient.id = :recipientId")
    List<NotificationResultDTO> findNotificationsByRecipientId(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("SELECT new org.snapgram.dto.NotificationResultDTO(e, r.isRead, t.actor.id, r.recipient.id) FROM NotificationEntity e " +
            "INNER JOIN NotificationRecipient r ON r.notificationEntity.id = e.id " +
            "INNER JOIN NotificationTrigger t ON t.notificationEntity.id = e.id " +
            "WHERE e.id = :id")
    NotificationResultDTO findNotificationById(UUID id);
}
