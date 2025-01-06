package org.snapgram.service.notification;

import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    void createNotification(CreateNotifyDTO notification);
    List<NotificationDTO> getNotificationsByUser(UUID userId, Pageable pageable);
    void deleteNotification(UUID notificationId);
    void markAsRead(UUID currentUserId);

    boolean isRead(UUID userId);
}
