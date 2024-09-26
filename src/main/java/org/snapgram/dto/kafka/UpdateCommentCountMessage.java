package org.snapgram.dto.kafka;

import java.io.Serializable;
import java.util.UUID;

public record UpdateCommentCountMessage(UUID postId, int commentCount) implements Serializable {
}
