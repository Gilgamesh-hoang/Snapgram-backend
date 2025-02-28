package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.snapgram.enums.ConversationType;
import org.snapgram.enums.MessageType;
import org.snapgram.validation.enum_pattern.EnumPattern;

import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {
    @NotNull
    private UUID senderId;

    private UUID conversationId;

    private UUID recipientId;

    @NotBlank
    @Length(max = 4000)
    private String content;

    @NotNull
    @EnumPattern(name = "contentType", regexp = "TEXT|IMAGE|VIDEO")
    private MessageType contentType;

    @NotNull
    @EnumPattern(name = "conversationType", regexp = "GROUP|USER")
    private ConversationType conversationType;
}
