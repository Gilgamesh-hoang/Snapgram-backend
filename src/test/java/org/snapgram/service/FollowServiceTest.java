package org.snapgram.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.snapgram.entity.database.Follow;
import org.snapgram.entity.database.User;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.kafka.producer.NewsfeedProducer;
import org.snapgram.repository.database.FollowRepository;
import org.snapgram.service.follow.FollowService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private NewsfeedProducer newsfeedProducer;

    @Mock
    private RedisProducer redisProducer;

    @InjectMocks
    private FollowService followService;

    private User createActiveUser(UUID userId) {
        User user = User.builder().isActive(true).isDeleted(false).build();
        user.setId(userId);
        return user;
    }

    @Test
    void followUser_shouldSaveFollow_whenNotAlreadyFollowing() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser(followeeId))
                .build();
        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.empty());

        followService.followUser(userId, followeeId);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void followUser_shouldNotSaveFollow_whenAlreadyFollowing() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser(followeeId))
                .build();
        Follow existingFollow = Follow.builder().isDeleted(false).build();

        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.of(existingFollow));

        followService.followUser(userId, followeeId);

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void unfollowUser_shouldMarkFollowAsDeleted_whenFollowingExists() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser(followeeId))
                .build();
        Follow existingFollow = Follow.builder().isDeleted(false).build();

        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.of(existingFollow));

        followService.unfollowUser(userId, followeeId);

        assertTrue(existingFollow.getIsDeleted());
        verify(followRepository).save(existingFollow);
    }

    @Test
    void unfollowUser_shouldNotMarkFollowAsDeleted_whenFollowDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(userId))
                .followee(createActiveUser(followeeId))
                .build();

        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.empty());

        followService.unfollowUser(userId, followeeId);

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void removeFollower_shouldMarkFollowAsDeleted_whenFollowerExists() {
        UUID userId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(followerId))
                .followee(createActiveUser(userId))
                .build();
        Follow existingFollow = Follow.builder().isDeleted(false).build();

        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.of(existingFollow));

        followService.removeFollower(userId, followerId);

        assertTrue(existingFollow.getIsDeleted());
        verify(followRepository).save(existingFollow);
    }

    @Test
    void removeFollower_shouldNotMarkFollowAsDeleted_whenFollowerDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        Follow follow = Follow.builder()
                .follower(createActiveUser(followerId))
                .followee(createActiveUser(userId))
                .build();
        when(followRepository.findOne(Example.of(follow))).thenReturn(Optional.empty());

        followService.removeFollower(userId, followerId);

        verify(followRepository, never()).save(any(Follow.class));
    }
}
