package org.snapgram.service.search;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.UserRepository;
import org.snapgram.repository.elasticsearch.user.ICustomUserElasticRepo;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService implements ISearchService {
    IRedisService redisService;
    IFollowService followService;
    ICustomUserElasticRepo customUserElastic;
    UserRepository userRepository;
    UserMapper userMapper;
    RedisProducer redisProducer;

    @Override
    public Set<UserDTO> searchFollowersByUser(UUID userId, String keyword, Pageable pageable) {
        String redisKey = RedisKeyUtil.getSearchFollowersKey(userId, keyword, pageable.getPageNumber(), pageable.getPageSize());

        List<UUID> followerIds = followService.getFollowersByUser(userId, Pageable.unpaged())
                .stream().map(UserDTO::getId).toList();
        if (followerIds.isEmpty()) {
            return Collections.emptySet();
        }
        return getAndCacheResults(redisKey, () -> customUserElastic.searchFollow(keyword, followerIds, pageable));
    }

    @Override
    public Set<UserDTO> searchFollowingByUser(UUID userId, String keyword, Pageable pageable) {
        String redisKey = RedisKeyUtil.getSearchFollowingKey(userId, keyword, pageable.getPageNumber(), pageable.getPageSize());
        List<UUID> followerIds = followService.getFolloweesByUser(userId, Pageable.unpaged())
                .stream().map(UserDTO::getId).toList();
        if (followerIds.isEmpty()) {
            return Collections.emptySet();
        }
        return getAndCacheResults(redisKey, () -> customUserElastic.searchFollow(keyword, followerIds, pageable));
    }

    @Override
    public Set<UserDTO> searchByKeyword(String keyword, Pageable page) {
        String redisKey = RedisKeyUtil.getSearchUserKey(keyword, page.getPageNumber(), page.getPageSize());
        return getAndCacheResults(redisKey, () -> customUserElastic.searchByKeyword(keyword, page));
    }

    private Set<UserDTO> getAndCacheResults(String redisKey, Supplier<Set<UserDocument>> elasticSearchSupplier) {
        // Try to fetch results from Redis
        Set<UserDTO> redisResults = redisService.getSet(redisKey);
        if (!redisResults.isEmpty()) {
            return redisResults;
        }

        // If Redis is empty, fetch from Elasticsearch
        Set<UserDocument> elasticResults = elasticSearchSupplier.get();

        // Map the results to UserDTO and fetch full user data from the database
        Set<UserDTO> results = userRepository.findAllById(elasticResults.stream()
                        .map(UserDocument::getId).toList())
                .stream().map(userMapper::toDTO).collect(Collectors.toSet());

        // Cache the results in Redis asynchronously
        redisProducer.sendSaveSet(redisKey, results,5L, TimeUnit.MINUTES);

        return results;
    }

}
