package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyCommentRequest {
    @NotNull(message = "Parent comment id is mandatory")
    private UUID parentCommentId;
    @NotBlank(message = "Content is mandatory")
    private String content;
}
