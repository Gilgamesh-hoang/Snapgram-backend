package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyCommentRequest {
    @NotNull
    private UUID postId;
    @NotNull
    private UUID parentCommentId;
    @NotBlank
    private String content;
}
