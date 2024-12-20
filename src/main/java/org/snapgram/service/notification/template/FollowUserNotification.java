package org.snapgram.service.notification.template;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.kafka.producer.NotificationProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowUserNotification extends NotificationTemplate {
    IUserService userService;

    public FollowUserNotification(NotificationEntityRepository entityRepository,
                                  NotificationRecipientRepository recipientRepository,
                                  NotificationTriggerRepository triggerRepository,
                                  NotificationProducer producer,
                                  IUserService userService) {
        super(entityRepository, recipientRepository, triggerRepository, producer);
        this.userService = userService;
    }


    @Override
    public NotificationDTO generateNotification(NotificationResultDTO notify) {
        return NotificationDTO.builder()
                .id(notify.getNotificationEntity().getId())
                .actor(userService.getCreatorById(notify.getActorId()))
                .type(notify.getNotificationEntity().getType())
                .isRead(notify.isRead())
                .createdAt(notify.getNotificationEntity().getCreatedAt())
                .build();
    }

    @Override
    protected UUID getRecipientId(CreateNotifyDTO notification) {
        return notification.getEntityId();
    }
}