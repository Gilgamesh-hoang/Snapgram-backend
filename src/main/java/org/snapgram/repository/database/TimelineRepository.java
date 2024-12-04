package org.snapgram.repository.database;

import org.snapgram.entity.database.timeline.Timeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TimelineRepository extends JpaRepository<Timeline, UUID> {
    @Modifying
    @Query("DELETE FROM Timeline t WHERE t.userId = :userId AND t.postId IN :postIds")
    void deleteAllByUserIdAndPostIds(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);

}
