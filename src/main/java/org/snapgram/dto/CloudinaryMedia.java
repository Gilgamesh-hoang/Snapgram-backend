package org.snapgram.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class CloudinaryMedia implements Serializable {
    @NotBlank(message = "Url is required")
    public String url;
    @NotBlank(message = "Public id is required")
    public String publicId;
    @NotBlank(message = "Signature is required")
    public String signature;
    @NotBlank(message = "version is required")
    public String version;
    @NotBlank(message = "Resource type is required")
    public String resourceType;
}
