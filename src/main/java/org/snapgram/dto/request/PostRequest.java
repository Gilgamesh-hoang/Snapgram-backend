package org.snapgram.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.validation.tag.ValidTags;

import java.util.List;
import java.util.UUID;

@Data
public class PostRequest {
    private UUID id;
    @Length(max = 2200)
    private String caption;
    @ValidTags
    private List<String> tags;
    private List<UUID> removeMedia;
    private List<CloudinaryMedia> media;
}
