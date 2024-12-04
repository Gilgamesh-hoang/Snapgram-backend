package org.snapgram.dto.kafka;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

public record PostCreatedMessage(UUID creator, UUID postId, Timestamp createdAt) implements Serializable {
}
