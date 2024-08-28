package org.snapgram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudinaryPojo {
    @JsonProperty("secure_url")
    private String secureUrl;
    @JsonProperty("public_id")
    private String publicId;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("asset_folder")
    private String assetFolder;
    @JsonProperty("resource_type")
    private String resourceType;
}
