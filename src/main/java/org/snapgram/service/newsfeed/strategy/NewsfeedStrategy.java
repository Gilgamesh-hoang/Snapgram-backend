package org.snapgram.service.newsfeed.strategy;

import org.snapgram.dto.response.PostDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NewsfeedStrategy {
    List<PostDTO> getNewsfeed(UUID userId, Pageable pageable);
}
