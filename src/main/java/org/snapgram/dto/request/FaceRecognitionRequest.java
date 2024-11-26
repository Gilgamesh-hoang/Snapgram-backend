package org.snapgram.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.snapgram.dto.CloudinaryMedia;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class FaceRecognitionRequest implements Serializable {
    @NotNull(message = "User id is required")
    private UUID userId;
    @NotEmpty(message = "Images are required")
    @Valid
    private List<CloudinaryMedia> images;
}
