package org.snapgram.repository.database;

import org.snapgram.entity.database.notification.NotificationTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface NotificationTriggerRepository extends JpaRepository<NotificationTrigger, UUID> {

    @Modifying
    void deleteByNotificationEntity_Id(UUID notificationId);
}
