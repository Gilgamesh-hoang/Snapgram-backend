package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostRequest {
    @NotBlank(message = "caption is mandatory")
    private String caption;
    private MultipartFile[] images;
}
