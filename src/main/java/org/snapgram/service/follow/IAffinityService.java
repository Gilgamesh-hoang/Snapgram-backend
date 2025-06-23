package org.snapgram.service.follow;

import org.snapgram.dto.AffinityDTO;

import java.util.UUID;

public interface IAffinityService {
   void increaseAffinityByLike(UUID currentUserId, UUID postId);

   void increaseAffinityByComment(UUID currentUserId, UUID postId);

   void increaseAffinity(UUID currentUserId, UUID otherUserId);

   void updateAffinity(AffinityDTO affinity);
}
