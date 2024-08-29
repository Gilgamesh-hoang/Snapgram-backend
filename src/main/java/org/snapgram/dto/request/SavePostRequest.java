package org.snapgram.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class SavePostRequest {
    @NotNull
    private UUID postId;
    @NotNull
    private Boolean isSaved;
}
