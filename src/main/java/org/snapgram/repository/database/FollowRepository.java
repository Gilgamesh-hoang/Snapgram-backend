package org.snapgram.repository.database;

import io.lettuce.core.dynamic.annotation.Param;
import org.snapgram.entity.database.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Query("SELECT f.followee.id FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id IN :followeeIds")
    List<UUID> findByFollowerIdAndFolloweeIdIn(@Param("followerId") UUID followerId, @Param("followeeIds") List<UUID> followeeIds);


}
