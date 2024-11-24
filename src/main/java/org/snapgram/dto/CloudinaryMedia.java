package org.snapgram.dto;

import lombok.Data;

@Data
public class CloudinaryMedia {
    private String url;
    private String publicId;
    private String signature;
    private String version;
    private String resourceType;
}
