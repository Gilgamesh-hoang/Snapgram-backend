package org.snapgram.service.notification.template;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.NotificationResultDTO;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.kafka.producer.NotificationProducer;
import org.snapgram.repository.database.NotificationEntityRepository;
import org.snapgram.repository.database.NotificationRecipientRepository;
import org.snapgram.repository.database.NotificationTriggerRepository;
import org.snapgram.service.comment.ICommentService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.user.IUserService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentPostNotification extends NotificationTemplate {
    IPostService postService;
    ICommentService commentService;
    IUserService userService;

    public CommentPostNotification(NotificationEntityRepository entityRepository,
                                   NotificationRecipientRepository recipientRepository,
                                   NotificationTriggerRepository triggerRepository,
                                   NotificationProducer producer,
                                   IPostService postService,
                                   ICommentService commentService,
                                   IUserService userService) {
        super(entityRepository, recipientRepository, triggerRepository, producer);
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
    }


    @Override
    public NotificationDTO generateNotification(NotificationResultDTO notify) {
        UUID commentId = notify.getNotificationEntity().getEntityId();
        CommentDTO comment = commentService.getCommentById(commentId);
        UUID postId = commentService.getPostIdByComment(commentId);
        return NotificationDTO.builder()
                .id(notify.getNotificationEntity().getId())
                .recipientId(notify.getRecipientId())
                .entityId(commentId)
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
        PostDTO post = commentService.getPostByComment(notification.getEntityId());
        return post.getCreator().getId();
    }
}