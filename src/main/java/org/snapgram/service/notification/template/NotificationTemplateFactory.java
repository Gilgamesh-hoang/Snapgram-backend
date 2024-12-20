package org.snapgram.service.notification.template;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.enums.NotificationType;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class NotificationTemplateFactory {
    ApplicationContext context;

    public NotificationTemplate getTemplate(NotificationType type) {
        return switch (type) {
            case COMMENT_POST -> context.getBean(CommentPostNotification.class);
            case FOLLOW_USER -> context.getBean(FollowUserNotification.class);
            case LIKE_COMMENT -> context.getBean(LikeCommentNotification.class);
            case LIKE_POST -> context.getBean(LikePostNotification.class);
            case REPLY_COMMENT -> context.getBean(ReplyCommentNotification.class);
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        };
    }
}
