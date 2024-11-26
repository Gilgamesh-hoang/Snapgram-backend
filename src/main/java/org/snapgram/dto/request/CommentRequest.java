package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    @NotNull(message = "Post id is mandatory")
    private UUID postId;
    @NotBlank(message = "Content is mandatory")
    private String content;
}
