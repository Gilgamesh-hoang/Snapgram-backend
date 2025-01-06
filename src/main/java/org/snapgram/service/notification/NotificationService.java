package org.snapgram.service.notification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.enums.NotificationType;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.snapgram.service.notification.template.NotificationTemplate;
import org.snapgram.service.notification.template.NotificationTemplateFactory;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService implements INotificationService {
    NotificationRecipientRepository recipientRepository;
    NotificationEntityRepository entityRepository;
    NotificationTriggerRepository triggerRepository;
    IRedisService redisService;
    RedisProducer redisProducer;
    NotificationTemplateFactory factory;

    @Override
    @Async
    @Transactional
    public void createNotification(CreateNotifyDTO notification) {

        NotificationTemplate template = factory.getTemplate(notification.getType());
        NotificationDTO res = template.createNotification(notification);

        String redisKey = RedisKeyUtil.getNotificationKey(res.getRecipientId(), 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));
    }

    @Override
    public List<NotificationDTO> getNotificationsByUser(UUID userId, Pageable pageable) {
        // Calculate the start and end indices for pagination
        String redisKey = RedisKeyUtil.getNotificationKey(userId, pageable.getPageNumber(), pageable.getPageSize());
        List<NotificationDTO> result = redisService.getList(redisKey, NotificationDTO.class);
        if (result != null) {
            return result;
        }


        // Fetch notifications from the database
        List<NotificationResultDTO> notifies = entityRepository.findNotificationsByRecipientId(userId, pageable);
        if (notifies.isEmpty()) {
            return List.of();
        }

        Map<NotificationType, NotificationTemplate> cache = Collections.synchronizedMap(new EnumMap<>(NotificationType.class));
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            result = notifies.stream()
                    .map(notify -> executor.submit(() ->
                            cache.computeIfAbsent(notify.getNotificationEntity().getType(), factory::getTemplate)
                                    .generateNotification(notify)))
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            log.error("Error while generating notification", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(NotificationDTO::getCreatedAt).reversed())
                    .toList();
        } finally {
            executor.shutdown();
        }

        // Cache the notifications
        redisProducer.sendSaveList(redisKey, result, null, null, null);
        return result;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        UUID currentUserId = UserSecurityHelper.getCurrentUser().getId();
        NotificationResultDTO notification = entityRepository.findNotificationById(notificationId);
        if (notification == null) {
            return;
        }
        if (!notification.getRecipientId().equals(currentUserId)) {
            throw new IllegalArgumentException("You are not authorized to delete this notification");
        }

        triggerRepository.deleteByNotificationEntity_Id(notificationId);
        recipientRepository.deleteByNotificationEntity_Id(notificationId);
        entityRepository.deleteById(notificationId);

        String redisKey = RedisKeyUtil.getNotificationKey(currentUserId, 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));
    }

    @Override
    @Transactional
    public void markAsRead(UUID currentUserId) {
        redisProducer.sendSaveMap(RedisKeyUtil.READ_NOTIFICATION, Map.of(currentUserId.toString(), true), null, null);
        recipientRepository.markAsRead(currentUserId);
    }

    @Override
    public boolean isRead(UUID userId) {
        try {
            return redisService.getEntryFromMap(RedisKeyUtil.READ_NOTIFICATION, userId.toString(), Boolean.class);
        } catch (NullPointerException e) {
            return false;
        }
    }

}
