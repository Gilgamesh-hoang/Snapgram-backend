package org.snapgram.service.timeline;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ITimelineService {
    List<PostDTO> getTimelinesByUser(UUID userId, Pageable pageable);

    CompletableFuture<Void> generateTimeline(UUID creatorId, UUID postId, Timestamp createdAt);

    /**
     * Thêm bài viết của người được follow (followee) vào timeline của follower.
     *
     * @param followerId ID của người theo dõi.
     * @param followeeId ID của người được theo dõi.
     */
    CompletableFuture<Void> addPostsToTimeline(UUID followerId, UUID followeeId);

    /**
     * Xóa bài viết của người bị unfollow (followee) khỏi timeline của follower.
     *
     * @param followerId ID của người theo dõi.
     * @param followeeId ID của người bị unfollow.
     */
    CompletableFuture<Void> removePostsFromTimeline(UUID followerId, UUID followeeId);
}
