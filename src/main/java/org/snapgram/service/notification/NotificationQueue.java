package org.snapgram.service.notification;

import org.snapgram.dto.response.NotificationDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationQueue {
    private final ConcurrentHashMap<UUID, NotificationDTO> queue = new ConcurrentHashMap<>();

    public void addNotification(NotificationDTO notification) {
        queue.put(notification.getId(), notification);
    }

    public List<NotificationDTO> drainQueue() {
        List<NotificationDTO> notifications = queue.values().stream().toList();
//        notificationQueue.clear();
        return notifications;
    }
}

