package org.snapgram.dto.kafka;

import java.io.Serializable;
import java.util.UUID;

public record FollowCreatedMessage(UUID followerId, UUID followeeId) implements Serializable { }
