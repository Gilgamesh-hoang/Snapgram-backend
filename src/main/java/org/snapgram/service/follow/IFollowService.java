package org.snapgram.service.follow;

import java.util.UUID;

public interface IFollowService {
    int countFollowers(UUID userId);
    int countFollowees(UUID userId);
}
