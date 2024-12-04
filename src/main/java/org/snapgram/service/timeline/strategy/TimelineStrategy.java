package org.snapgram.service.timeline.strategy;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TimelineStrategy {
    List<PostDTO> getTimeline(UUID userId, Pageable pageable);
}
