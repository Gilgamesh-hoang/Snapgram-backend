package org.snapgram.service.follow;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.Follow;
import org.snapgram.entity.database.user.User;
import org.snapgram.kafka.producer.AffinityProducer;
import org.snapgram.repository.database.FollowRepository;
import org.snapgram.service.post.IPostService;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AffinityService implements IAffinityService {
    IPostService postService;
    AffinityProducer affinityProducer;
    FollowRepository followRepository;

    @Override
    @Async
    public void increaseAffinityByLike(UUID currentUserId, UUID postId) {
        processWithPost(currentUserId, postId);
    }

    @Override
    @Async
    public void increaseAffinityByComment(UUID currentUserId, UUID postId) {
        processWithPost(currentUserId, postId);
    }

    private void processWithPost(UUID currentUserId, UUID postId) {
        PostDTO post = postService.getPostById(postId);
        if (post != null) {
            affinityProducer.sendAffinityMessage(currentUserId, post.getCreator().getId());
        }
    }

    @Override
    @Async
    public void increaseAffinity(UUID otherUserId) {
        UUID currentUserId = UserSecurityHelper.getCurrentUser().getId();
        increaseAffinity(currentUserId, otherUserId);
    }

    @Override
    public void increaseAffinity(UUID currentUserId, UUID otherUserId) {
        affinityProducer.sendAffinityMessage(currentUserId, otherUserId);
    }

    @Override
    public void updateAffinity(AffinityDTO affinity) {
        final float RATE = 0.1f;
        Follow follow = Follow.builder()
                .follower(User.builder().id(affinity.getFollowerId()).build())
                .followee(User.builder().id(affinity.getFolloweeId()).build())
                .isDeleted(false)
                .build();
        Follow entity = followRepository.findOne(Example.of(follow)).orElse(null);
        if (entity == null) {
            return;
        }
        float closeness = entity.getCloseness() + (affinity.getCloseness() * RATE);
        entity.setCloseness(closeness);
        followRepository.save(entity);
    }
}
