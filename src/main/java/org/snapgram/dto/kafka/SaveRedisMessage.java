package org.snapgram.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SaveRedisMessage implements Serializable {
    private String redisKey;
//    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private Object obj;
    private Long timeout;
    private TimeUnit timeUnit;
    private Integer index;

    public SaveRedisMessage(String redisKey, Object obj, Long timeout, TimeUnit timeUnit) {
        this.redisKey = redisKey;
        this.obj = obj;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    public SaveRedisMessage(String redisKey, Object obj, Long timeout, TimeUnit timeUnit, Integer index) {
        this.redisKey = redisKey;
        this.obj = obj;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.index = index;
    }
}