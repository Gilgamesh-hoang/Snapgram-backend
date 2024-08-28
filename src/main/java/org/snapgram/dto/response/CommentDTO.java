package org.snapgram.dto.response;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class CommentDTO {
    private UUID id;
    private CreatorDTO creator;
    private String content;
    private CommentDTO parentComment;
    private Timestamp createdAt;
    private Integer likeCount = 0;

}
