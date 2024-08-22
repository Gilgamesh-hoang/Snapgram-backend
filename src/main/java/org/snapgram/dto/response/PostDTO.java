package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.snapgram.entity.database.Tag;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PostDTO implements Serializable {

    private UUID id;
    private String caption;
    private List<PostMediaDTO> media;
    private List<TagDTO> tags;
    private Integer likeCount = 0;
    private Integer commentCount = 0;
    @JsonProperty("isLiked")
    private boolean isLiked;
    private Timestamp createdAt;
}
