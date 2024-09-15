package org.snapgram.service.follow;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.Follow;
import org.snapgram.entity.database.User;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.FollowRepository;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FollowService implements IFollowService {
    FollowRepository followRepository;
    UserMapper userMapper;
    IRedisService redisService;

    @Override
    public int countFollowers(UUID userId) {
        Example<Follow> example = createFollowerExample(userId);
        return (int) followRepository.count(example);
    }

    private Example<Follow> createFollowerExample(UUID userId) {
        return Example.of(Follow.builder()
                .followee(createActiveUser(userId))
                .follower(createActiveUser())
                .build());
    }

    private Example<Follow> createFollowingExample(UUID userId) {
        return Example.of(Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser())
                .build());
    }

    private User createActiveUser() {
        return User.builder().isActive(true).isDeleted(false).build();
    }
    private User createActiveUser(UUID userId) {
        User user = createActiveUser();
        user.setId(userId);
        return user;
    }

    @Override
    public int countFollowees(UUID userId) {
        Example<Follow> example = createFollowingExample(userId);
        return (int) followRepository.count(example);
    }

    @Override
    public List<UserDTO> getFollowersByUser(UUID userId, Pageable pageable) {
        int page, size;
        try {
            page = pageable.getPageNumber();
            size = pageable.getPageSize();
        } catch (UnsupportedOperationException e) {
            // unpaged
            page = size = -1;
        }
        String redisKey = RedisKeyUtil.getUserFollowersKey(userId, page, size);
        List<UserDTO> result = redisService.getList(redisKey);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        Example<Follow> example = createFollowerExample(userId);
        return getAndCacheFollowData(example, pageable, redisKey, Follow::getFollower);
    }

    @Override
    public List<UserDTO> getFollowingByUser(UUID userId, Pageable pageable) {
        int page, size;
        try {
            page = pageable.getPageNumber();
            size = pageable.getPageSize();
        } catch (UnsupportedOperationException e) {
            // unpaged
            page = size = -1;
        }
        String redisKey = RedisKeyUtil.getUserFollowingKey(userId, page, size);
        List<UserDTO> result = redisService.getList(redisKey);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        Example<Follow> example = createFollowingExample(userId);
        return getAndCacheFollowData(example, pageable, redisKey, Follow::getFollowee);
    }

    @Override
    public void followUser(UUID userId, UUID followeeId) {
        Follow follow = Follow.builder()
                .follower(User.builder().id(userId).build())
                .followee(createActiveUser(followeeId))
                .build();
        if (followRepository.exists(Example.of(follow))) {
            return;
        }
        followRepository.save(follow);
    }

    @Override
    @Transactional
    public void unfollowUser(UUID userId, UUID followeeId) {
        Follow follow = Follow.builder()
                .follower(User.builder().id(userId).build())
                .followee(User.builder().id(followeeId).build())
                .build();
        follow = followRepository.findOne(Example.of(follow)).orElse(null);
        if (follow != null) {
            followRepository.delete(follow);
        }
    }

    @Override
    public void removeFollower(UUID userId, UUID followerId) {
        Follow follow = Follow.builder()
                .follower(User.builder().id(followerId).build())
                .followee(User.builder().id(userId).build())
                .build();
        follow = followRepository.findOne(Example.of(follow)).orElse(null);
        if (follow != null) {
            followRepository.delete(follow);
        }
    }

    @Override
    public List<UUID> getFollowedUserIds(UUID currentUserId, List<UUID> userIds) {
        return followRepository.findByFollowerIdAndFolloweeIdIn(currentUserId, userIds);
    }

    private List<UserDTO> getAndCacheFollowData(Example<Follow> example, Pageable pageable, String redisKey, Function<Follow, User> userMapperFunction) {
        // Fetch from the database and map to User
        List<User> follows = followRepository.findAll(example, pageable).getContent()
                .stream().map(userMapperFunction).toList();

        // Map Users to DTOs
        List<UserDTO> result = userMapper.toDTOs(follows);

        // Cache the result in Redis
        redisService.saveList(redisKey, result);
        redisService.setTimeout(redisKey, 1, result.isEmpty() ? TimeUnit.MINUTES : TimeUnit.DAYS);
        return result;
    }

}
