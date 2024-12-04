package org.snapgram.service.follow;

import org.snapgram.dto.response.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IFollowService {
    int countFollowers(UUID userId);

    int countFollowees(UUID userId);

    List<UserDTO> getFollowersByUser(UUID userId, Pageable pageable);

    List<UserDTO> getFolloweesByUser(UUID userId, Pageable pageable);

    void followUser(UUID userId, UUID followeeId);

    void unfollowUser(UUID userId, UUID followeeId);

    void removeFollower(UUID userId, UUID followerId);

    List<UUID> getFollowedUserIds(UUID currentUserId, List<UUID> userIds);
}
