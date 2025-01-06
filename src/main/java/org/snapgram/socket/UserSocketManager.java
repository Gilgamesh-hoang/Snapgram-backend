package org.snapgram.socket;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class UserSocketManager {

    private final Map<UUID, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();

    public void addUserSocket(UUID userId, SocketIOClient client) {
        userSocketMap.put(userId, client);
    }

    public SocketIOClient getUserSocket(UUID userId) {
        return userSocketMap.get(userId);
    }

    public boolean containsUser(UUID userId) {
        return userSocketMap.containsKey(userId);
    }

    public List<SocketIOClient> getAllSockets() {
        return List.copyOf(userSocketMap.values());
    }

    public UUID removeSocket(SocketIOClient client) {
        UUID userIdToRemove = null;
        for (Map.Entry<UUID, SocketIOClient> entry : userSocketMap.entrySet()) {
            if (entry.getValue().equals(client)) {
                userIdToRemove = entry.getKey();
                userSocketMap.remove(entry.getKey());
                break;
            }
        }
        return userIdToRemove;
    }
}

