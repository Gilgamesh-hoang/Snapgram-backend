package org.snapgram.dto.kafka;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public record SaveRedisMessage(String redisKey, Object obj, Long timeout, TimeUnit timeUnit) implements Serializable {
}
