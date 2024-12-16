package org.snapgram.service.follow;

import org.snapgram.dto.AffinityDTO;

import java.util.UUID;

public interface IAffinityService {
   void increaseAffinityByLike(UUID postId);

   void increaseAffinityByComment(UUID postId);

   void increaseAffinity(UUID otherUserId);

   void updateAffinity(AffinityDTO affinity);
}
