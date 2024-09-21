package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditCommentRequest {
    @NotBlank
    private String content;
}
