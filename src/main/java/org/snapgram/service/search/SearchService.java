package org.snapgram.service.search;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.UserRepository;
import org.snapgram.repository.elasticsearch.user.ICustomUserElasticRepo;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService implements ISearchService {
    IRedisService redisService;
    ICustomUserElasticRepo customUserElastic;
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public Set<UserDTO> searchByKeyword(String keyword, Pageable page) {
        String redisKey = RedisKeyUtil.getSearchUserKey(keyword, page.getPageNumber(), page.getPageSize());
        Set<UserDTO> results = new HashSet<>();

        Set<UserDTO> redisResults = redisService.getSet(redisKey);
        if (!redisResults.isEmpty()) {
            return redisResults;
        }

        Set<UserDocument> elasticResults = customUserElastic.searchByKeyword(keyword, page);
        userRepository.findAllById(elasticResults.stream().map(UserDocument::getId).toList())
                .forEach(user -> results.add(userMapper.toDTO(user)));

        CompletableFuture.runAsync(() -> {
            redisService.saveSet(redisKey, results);
            if (results.isEmpty())
                redisService.setTimeout(redisKey, 5, TimeUnit.MINUTES);
            else
                redisService.setTimeout(redisKey, 1, TimeUnit.HOURS);
        });
        return results;
    }
}
