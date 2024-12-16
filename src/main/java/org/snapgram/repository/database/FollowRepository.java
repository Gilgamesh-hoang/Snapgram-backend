package org.snapgram.repository.database;

import io.lettuce.core.dynamic.annotation.Param;
import org.snapgram.entity.database.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Query("SELECT f.followee.id FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id IN :followeeIds AND f.isDeleted = false")
    List<UUID> findByFollowerIdAndFolloweeIdIn(@Param("followerId") UUID followerId, @Param("followeeIds") List<UUID> followeeIds);

    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id IN :followeeIds AND f.isDeleted = false")
    List<Follow> getAffinities(@Param("followerId") UUID followerId, @Param("followeeIds") Set<UUID> followeeIds);
}
