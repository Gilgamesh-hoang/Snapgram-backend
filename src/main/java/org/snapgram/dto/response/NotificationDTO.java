package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.snapgram.enums.NotificationType;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO implements Serializable {
    private UUID id;
    private UUID recipientId;
    private UUID entityId;
    private CreatorDTO actor;
    private NotificationType type;
    @JsonProperty("isRead")
    private boolean isRead;
    private Timestamp createdAt;
    private String content;
    private Map<String, Object> options;
}
