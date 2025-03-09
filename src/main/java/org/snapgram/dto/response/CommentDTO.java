package org.snapgram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.snapgram.enums.SentimentType;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO implements Serializable {
    private UUID id;
    private CreatorDTO creator;
    private String content;
    private UUID parentCommentId;
    private Timestamp createdAt;
    private Integer likeCount = 0;
    private Integer level;
    private int replyCount;
    private SentimentType sentiment;
}
