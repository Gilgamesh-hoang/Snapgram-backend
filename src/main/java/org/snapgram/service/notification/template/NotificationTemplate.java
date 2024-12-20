package org.snapgram.service.notification.template;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.entity.database.notification.NotificationEntity;
import org.snapgram.entity.database.notification.NotificationRecipient;
import org.snapgram.entity.database.notification.NotificationTrigger;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.NotificationType;
import org.snapgram.kafka.producer.NotificationProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class NotificationTemplate {
    NotificationEntityRepository entityRepository;
    NotificationRecipientRepository recipientRepository;
    NotificationTriggerRepository triggerRepository;
    NotificationProducer producer;

    public NotificationDTO createNotification(CreateNotifyDTO notification) {
        NotificationEntity entity = getOrCreateNotificationEntity(notification.getEntityId(), notification.getType());
        NotificationRecipient recipient = getOrCreateNotificationRecipient(entity.getId(), getRecipientId(notification));
        NotificationTrigger trigger = updateNotificationTrigger(entity.getId(), notification.getActorId());
        producer.sendNotificationMessage(recipient.getRecipient().getId());
        return generateNotification(NotificationResultDTO.builder()
                .notificationEntity(entity)
                .isRead(recipient.getIsRead())
                .actorId(trigger.getActor().getId())
                .build());
    }

    public abstract NotificationDTO generateNotification(NotificationResultDTO notify);


    protected abstract UUID getRecipientId(CreateNotifyDTO notification);

    protected NotificationEntity getOrCreateNotificationEntity(UUID entityId, NotificationType type) {
        NotificationEntity notification = NotificationEntity.builder()
                .entityId(entityId)
                .type(type)
                .build();
        return entityRepository.findOne(Example.of(notification))
                .map(entity -> {
                    entity.setCreatedAt(Timestamp.from(Instant.now()));
                    return entityRepository.save(entity);
                })
                .orElseGet(() -> {
                    notification.setCreatedAt(Timestamp.from(Instant.now()));
                    return entityRepository.save(notification);
                });
    }

    protected NotificationRecipient getOrCreateNotificationRecipient(UUID entityId, UUID recipientId) {
        NotificationRecipient recipient = NotificationRecipient.builder()
                .notificationEntity(NotificationEntity.builder().id(entityId).build())
                .recipient(User.builder().id(recipientId).build())
                .build();
        return recipientRepository.findOne(Example.of(recipient))
                .orElseGet(() -> recipientRepository.save(recipient));
    }

    protected NotificationTrigger updateNotificationTrigger(UUID entityId, UUID actorId) {
        NotificationTrigger notification = NotificationTrigger.builder()
                .notificationEntity(NotificationEntity.builder().id(entityId).build())
                .build();
        return triggerRepository.findOne(Example.of(notification))
                .map(res -> {
                    if (!res.getActor().getId().equals(actorId)) {
                        res.setActor(User.builder().id(actorId).build());
                        return triggerRepository.save(res);
                    }
                    return res;
                })
                .orElseGet(() -> {
                    notification.setActor(User.builder().id(actorId).build());
                    return triggerRepository.save(notification);
                });
    }
}