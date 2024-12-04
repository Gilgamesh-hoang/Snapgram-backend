package org.snapgram.service.timeline.strategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.timeline.Timeline;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.repository.database.TimelineRepository;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActiveUserTimelineStrategy implements TimelineStrategy {
    final TimelineRepository timelineRepository;
    final IRedisService redisService;
    final RedisProducer redisProducer;
    final IPostService postService;

    @Override
    public List<PostDTO> getTimeline(UUID userId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        String redisKey = RedisKeyUtil.getTimelineKey(userId, page, size);
        List<PostDTO> results = redisService.getList(redisKey, PostDTO.class);
        if (results != null && !results.isEmpty()) {
            return results;
        }

        Example<Timeline> example = Example.of(Timeline.builder().userId(userId).build());
        Pageable timelinePageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("postCreatedAt")));
        List<UUID> postIds = timelineRepository.findAll(example, timelinePageable).stream().map(Timeline::getPostId).toList();
        results = postService.getPostsByIds(postIds);

        if (results.isEmpty()) {
            redisProducer.sendSaveList(redisKey, List.of(), 30, TimeUnit.SECONDS);
        } else {
//            timelineRepository.deleteAllByUserIdAndPostIds(userId, postIds);
            redisProducer.sendSaveList(redisKey, results, 1, TimeUnit.MINUTES);
        }

        return results;
    }

}
