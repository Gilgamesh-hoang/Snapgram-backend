package org.snapgram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.snapgram.entity.database.notification.NotificationEntity;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class NotificationResultDTO {
    private NotificationEntity notificationEntity;
    private boolean isRead;
    private UUID actorId;

}
