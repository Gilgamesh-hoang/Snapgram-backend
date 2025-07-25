package org.snapgram.service.notification.template;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.kafka.producer.NotificationProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.snapgram.service.comment.ICommentService;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeCommentNotification extends NotificationTemplate {
    ICommentService commentService;
    IUserService userService;

    public LikeCommentNotification(NotificationEntityRepository entityRepository,
                                   NotificationRecipientRepository recipientRepository,
                                   NotificationTriggerRepository triggerRepository,
                                   NotificationProducer producer,
                                   ICommentService commentService,
                                   IUserService userService) {
        super(entityRepository, recipientRepository, triggerRepository, producer);
        this.commentService = commentService;
        this.userService = userService;
    }

    @Override
    public NotificationDTO generateNotification(NotificationResultDTO notify) {
        CommentDTO comment = commentService.getCommentById(notify.getNotificationEntity().getEntityId());
        UUID postId = commentService.getPostIdByComment(comment.getId());
        return NotificationDTO.builder()
                .id(notify.getNotificationEntity().getId())
                .recipientId(notify.getRecipientId())
                .entityId(comment.getId())
                .actor(userService.getCreatorById(notify.getActorId()))
                .type(notify.getNotificationEntity().getType())
                .isRead(notify.isRead())
                .createdAt(notify.getNotificationEntity().getCreatedAt())
                .content(cutContent(comment.getContent()))
                .options(Map.of("postId", postId))
                .build();
    }

    @Override
    protected UUID getRecipientId(CreateNotifyDTO notification) {
        return commentService.getCommentById(notification.getEntityId()).getCreator().getId();
    }
}