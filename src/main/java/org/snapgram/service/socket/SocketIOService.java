package org.snapgram.service.socket;

import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import org.springframework.stereotype.Service;

@Service
public class SocketIOService {

//    private final SocketIoNamespace namespace;

//    public SocketIOService(SocketIoServer server) {
//        this.namespace = server.namespace("/notification");
//    }
//
//    /**
//     * Send a message to all connected clients.
//     *
//     * @param message The message to send.
//     */
//    public void sendMessageToAllClients(String message) {
//        namespace.send("message", message);
//    }
}