package org.snapgram.dto.kafka;

import java.io.Serializable;
import java.util.List;

public record DeleteRedisMessage(String redisKey, List<?> obj) implements Serializable {
}
