package org.snapgram.service.follow;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.dto.CreateNotifyDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.Follow;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.NotificationType;
import org.snapgram.kafka.producer.NewsfeedProducer;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.mapper.FollowMapper;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.FollowRepository;
import org.snapgram.service.notification.INotificationService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
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
    FollowMapper followMapper;
    IRedisService redisService;
    NewsfeedProducer newsfeedProducer;
    RedisProducer redisProducer;
    INotificationService notificationService;

    @Override
    public int countFollowers(UUID userId) {
        Example<Follow> example = createFollowerExample(userId);
        return (int) followRepository.count(example);
    }

    private Example<Follow> createFollowerExample(UUID userId) {
        return Example.of(Follow.builder()
                .followee(createActiveUser(userId))
                .follower(createActiveUser())
                .isDeleted(false)
                .build());
    }

    private Example<Follow> createFollowingExample(UUID userId) {
        return Example.of(Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser())
                .isDeleted(false)
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
        List<UserDTO> result = redisService.getList(redisKey, UserDTO.class);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        Example<Follow> example = createFollowerExample(userId);
        return getAndCacheFollowData(example, pageable, redisKey, Follow::getFollower);
    }

    @Override
    public List<UserDTO> getFolloweesByUser(UUID userId, Pageable pageable) {
        int page, size;
        try {
            page = pageable.getPageNumber();
            size = pageable.getPageSize();
        } catch (UnsupportedOperationException e) {
            // unpaged
            page = size = -1;
        }
        String redisKey = RedisKeyUtil.getUserFollowingKey(userId, page, size);
        List<UserDTO> result = redisService.getList(redisKey, UserDTO.class);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        Example<Follow> example = createFollowingExample(userId);
        return getAndCacheFollowData(example, pageable, redisKey, Follow::getFollowee);
    }

    @Override
    @Transactional
    public void followUser(UUID userId, UUID followeeId) {
        Follow follow = Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser(followeeId))
                .build();

        Follow entity = followRepository.findOne(Example.of(follow)).orElse(null);
        if (entity != null && !entity.getIsDeleted()) {
            return;
        }
        follow.setIsDeleted(false);
        followRepository.save(follow);
        newsfeedProducer.sendFollowCreatedMessage(userId, followeeId);

        String followingKey = RedisKeyUtil.getUserFollowingKey(userId, 0, 0);
        redisProducer.sendDeleteByKey(followingKey.substring(0, followingKey.indexOf("page")));

        String followersKey = RedisKeyUtil.getUserFollowersKey(followeeId, 0, 0);
        redisProducer.sendDeleteByKey(followersKey.substring(0, followersKey.indexOf("page")));

        notificationService.createNotification(CreateNotifyDTO.builder()
                .type(NotificationType.FOLLOW_USER)
                .entityId(follow.getFollowee().getId())
                .actorId(follow.getFollower().getId())
                .build());
    }

    @Override
    @Transactional
    public void unfollowUser(UUID userId, UUID followeeId) {
        processUnFollow(userId, followeeId, false);

        String followingKey = RedisKeyUtil.getUserFollowingKey(userId, 0, 0);
        redisProducer.sendDeleteByKey(followingKey.substring(0, followingKey.indexOf("page")));

        String followersKey = RedisKeyUtil.getUserFollowersKey(followeeId, 0, 0);
        redisProducer.sendDeleteByKey(followersKey.substring(0, followersKey.indexOf("page")));
    }

    @Override
    @Transactional
    public void removeFollower(UUID userId, UUID followerId) {
        processUnFollow(followerId, userId, true);

        String followingKey = RedisKeyUtil.getUserFollowingKey(followerId, 0, 0);
        redisProducer.sendDeleteByKey(followingKey.substring(0, followingKey.indexOf("page")));

        String followersKey = RedisKeyUtil.getUserFollowersKey(userId, 0, 0);
        redisProducer.sendDeleteByKey(followersKey.substring(0, followersKey.indexOf("page")));
    }

    private void processUnFollow(UUID followerId, UUID followeeId, boolean isRemoveFollower) {
        Follow follow = Follow.builder()
                .follower(createActiveUser(followerId))
                .followee(createActiveUser(followeeId))
                .build();
        Follow entity = followRepository.findOne(Example.of(follow)).orElse(null);
        if (entity == null || entity.getIsDeleted()) {
            return;
        }
        entity.setIsDeleted(true);
        followRepository.save(entity);

        if (isRemoveFollower) {
            newsfeedProducer.sendUnfollowMessage(followerId, followeeId);
        } else {
            newsfeedProducer.sendUnfollowMessage(followeeId, followerId);
        }
    }


    @Override
    public List<UUID> getFollowedUserIds(UUID currentUserId, List<UUID> userIds) {
        return followRepository.findByFollowerIdAndFolloweeIdIn(currentUserId, userIds);
    }

    @Override
    public List<AffinityDTO> getAffinities(UUID followerId, Set<UUID> followeeIds) {
        List<Follow> follows = followRepository.getAffinities(followerId, followeeIds);
        return followMapper.toAffinities(follows);
    }

    private List<UserDTO> getAndCacheFollowData(Example<Follow> example, Pageable pageable, String redisKey, Function<Follow, User> userMapperFunction) {
        // Fetch from the database and map to User
        List<User> follows = followRepository.findAll(example, pageable).getContent()
                .stream().map(userMapperFunction).toList();

        // Map Users to DTOs
        List<UserDTO> result = userMapper.toDTOs(follows);

        // Cache the result in Redis
        redisService.saveList(redisKey, result, null);
        redisService.setTTL(redisKey, 1, result.isEmpty() ? TimeUnit.MINUTES : TimeUnit.DAYS);
        return result;
    }

}
