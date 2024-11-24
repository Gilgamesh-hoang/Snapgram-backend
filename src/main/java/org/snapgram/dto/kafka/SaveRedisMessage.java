package org.snapgram.dto.kafka;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public record SaveRedisMessage(
        String redisKey,
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        Object obj,
        Long timeout,
        TimeUnit timeUnit
) implements Serializable {
}