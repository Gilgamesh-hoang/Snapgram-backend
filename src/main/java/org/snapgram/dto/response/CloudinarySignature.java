package org.snapgram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignature implements Serializable {
    private String apiKey;
    private long timestamp;
    private String folder;
    private String signature;
    private String publicId;
}
