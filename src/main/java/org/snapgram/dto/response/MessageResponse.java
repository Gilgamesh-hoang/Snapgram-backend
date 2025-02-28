package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.snapgram.enums.MessageType;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private UUID id;
    private CreatorDTO sender;
    private CreatorDTO recipient;
    private String content;
    private MessageType contentType;
    @JsonProperty("isRead")
    private boolean isRead;
    private Timestamp createdAt;
    private ConversationDTO conversation;
}
