package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditCommentRequest {
    @NotBlank(message = "Content is mandatory")
    private String content;
}
