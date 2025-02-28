package org.snapgram.socket;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class UserSocketManager {

    private final Map<UUID, List<SocketIOClient>> userSocketMap = new ConcurrentHashMap<>();

    /**
     * Thêm socket vào danh sách của người dùng.
     */
    public void addUserSocket(UUID userId, SocketIOClient client) {
        List<SocketIOClient> sockets = userSocketMap.get(userId);
        if (sockets == null) {
            CopyOnWriteArrayList<SocketIOClient> newSockets = new CopyOnWriteArrayList<>();
            newSockets.add(client);
            userSocketMap.put(userId, newSockets);
        } else {
            if (sockets.stream().noneMatch(socket -> socket.getSessionId().equals(client.getSessionId()))) {
                sockets.add(client);
            }
        }
    }

    /**
     * Lấy danh sách socket của một người dùng.
     */
    public List<SocketIOClient> getUserSockets(UUID userId) {
        return userSocketMap.getOrDefault(userId, Collections.emptyList());
    }

    /**
     * Kiểm tra người dùng có kết nối socket hay không.
     */
    public boolean containsUser(UUID userId) {
        return userSocketMap.containsKey(userId) && !userSocketMap.get(userId).isEmpty();
    }

    /**
     * Lấy tất cả socket của tất cả người dùng.
     */
    public List<SocketIOClient> getAllSockets() {
        return userSocketMap.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Xóa socket của một người dùng. Nếu không còn socket nào, xóa luôn userId khỏi map.
     */
    public UUID removeSocket(SocketIOClient client) {
        UUID userIdToRemove = null;

        for (Map.Entry<UUID, List<SocketIOClient>> entry : userSocketMap.entrySet()) {
            List<SocketIOClient> sockets = entry.getValue();
            if (sockets.remove(client)) {
                userIdToRemove = entry.getKey();
                if (sockets.isEmpty()) {
                    userSocketMap.remove(userIdToRemove);
                }
                break;
            }
        }
        return userIdToRemove;
    }

    /**
     * Lấy danh sách socket của nhiều user.
     */
    public List<SocketIOClient> getUserSocketsByUserIds(List<UUID> userIds) {
        return userIds.stream()
                .flatMap(userId -> getUserSockets(userId).stream())
                .toList();
    }
}
