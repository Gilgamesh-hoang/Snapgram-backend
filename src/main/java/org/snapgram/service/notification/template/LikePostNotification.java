package org.snapgram.service.notification.template;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.PostLikeDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.kafka.producer.NotificationProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.snapgram.service.post.IPostLikeService;
import org.snapgram.service.post.IPostService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikePostNotification extends NotificationTemplate {
    IPostService postService;
    IPostLikeService postLikeService;

    public LikePostNotification(NotificationEntityRepository notificationEntityRepository, NotificationRecipientRepository notificationRecipientRepository, NotificationTriggerRepository notificationTriggerRepository, NotificationProducer notificationProducer, IPostService postService, IPostLikeService postLikeService) {
        super(notificationEntityRepository, notificationRecipientRepository, notificationTriggerRepository, notificationProducer);
        this.postService = postService;
        this.postLikeService = postLikeService;
    }

    @Override
    public NotificationDTO generateNotification(NotificationResultDTO notify) {
        UUID postId = notify.getNotificationEntity().getEntityId();
        PostLikeDTO postLike = postLikeService.getPostLike(postId, notify.getActorId());
        return NotificationDTO.builder()
                .id(notify.getNotificationEntity().getId())
                .recipientId(notify.getRecipientId())
                .entityId(postId)
                .actor(postLike.getUser())
                .type(notify.getNotificationEntity().getType())
                .isRead(notify.isRead())
                .createdAt(notify.getNotificationEntity().getCreatedAt())
                .content(cutContent(postLike.getPost().getCaption()))
                .build();
    }

    @Override
    protected UUID getRecipientId(CreateNotifyDTO notification) {
        return postService.getPostById(notification.getEntityId()).getCreator().getId();
    }
}