package org.snapgram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.ConversationType;
import org.snapgram.enums.MessageType;

import java.sql.Timestamp;
import java.util.UUID;


@Builder
@Data
@NoArgsConstructor
public class MessageResultDTO {
    private UUID id;
    private User sender;
    private User recipient;
    private String content;
    private MessageType contentType;
    private Timestamp createdAt;
    private UUID conversationId;
    private ConversationType conversationType;
    private String conversationName;
    private boolean isRead;

    public MessageResultDTO(UUID id, User sender, User recipient, String content, MessageType contentType,
                            Timestamp createdAt, UUID conversationId, ConversationType conversationType,
                            String conversationName, boolean isRead) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.conversationId = conversationId;
        this.conversationType = conversationType;
        this.conversationName = conversationName;
        this.isRead = isRead;
    }



    public MessageResultDTO(UUID id, User sender, String content, MessageType contentType, Timestamp createdAt,
                            UUID conversationId, ConversationType conversationType, String conversationName) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.conversationId = conversationId;
        this.conversationType = conversationType;
        this.conversationName = conversationName;
    }
}
