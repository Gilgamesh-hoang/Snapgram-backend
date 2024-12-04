package org.snapgram.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.service.timeline.ITimelineService;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${API_PREFIX}/timeline")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimelineController {
    ITimelineService timelineService;
    RedisProducer redisProducer;

    @GetMapping
    public ResponseObject<List<PostDTO>> getCommentsByPost(
            @AuthenticationPrincipal CustomUserSecurity user,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") @Min(0) @Max(30) Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        List<PostDTO> timelinesByUser = timelineService.getTimelinesByUser(user.getId(), pageable);

        redisProducer.sendSaveMap(RedisKeyUtil.GET_TIMELINE_LATEST,
                Map.of(user.getId(), new Timestamp(System.currentTimeMillis())));

        return new ResponseObject<>(HttpStatus.OK, timelinesByUser);
    }

}
