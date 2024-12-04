package org.snapgram.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.service.user.IUserSyncDataService;
import org.snapgram.util.DebeziumConstant;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSyncDataConsumer {
    IUserSyncDataService userSyncDataService;

    @KafkaListener(topics = "db.snapgram.user")
    public void listen(JsonNode jsonNode) {
        if (jsonNode == null) {
            return;
        }

        UUID id;
        JsonNode payload = jsonNode.path("payload");
        String action = payload.path("op").asText();

        switch (action) {
            case DebeziumConstant.DEBEZIUM_CREATE, DebeziumConstant.DEBEZIUM_READ:
                id = UUID.fromString(payload.path("after").path("id").asText());
                userSyncDataService.createUser(id);
                break;
            case DebeziumConstant.DEBEZIUM_UPDATE:
                id = UUID.fromString(payload.path("after").path("id").asText());
                userSyncDataService.updateUser(id);
                break;
            case DebeziumConstant.DEBEZIUM_DELETE:
                id = UUID.fromString(payload.path("before").path("id").asText());
                userSyncDataService.deleteUser(id);
                break;
            default:
                break;
        }
    }
}
