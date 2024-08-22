package org.snapgram.service.follow;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.entity.database.Follow;
import org.snapgram.entity.database.User;
import org.snapgram.repository.database.FollowRepository;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FollowService implements IFollowService{
    FollowRepository followRepository;


    @Override
    public int countFollowers(UUID userId) {
        Example<Follow> example = Example.of(
                Follow.builder().followee(User.builder().id(userId).isActive(true).isDeleted(false).build()).build());
        return (int) followRepository.count(example);
    }

    @Override
    public int countFollowees(UUID userId) {
        Example<Follow> example = Example.of(
                Follow.builder().follower(User.builder().id(userId).isActive(true).isDeleted(false).build()).build());
        return (int) followRepository.count(example);
    }
}
