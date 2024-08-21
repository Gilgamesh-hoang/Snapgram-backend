package org.snapgram.dto.response;

import lombok.Data;
import org.snapgram.entity.database.Tag;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class PostDTO {

    private UUID id;

    private String caption;
    private List<PostMediaDTO> media;
    private List<TagDTO> tags;
    private Integer likeCount = 0;

    private Timestamp createdAt;
}
