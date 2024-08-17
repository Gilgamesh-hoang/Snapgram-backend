package org.snapgram.repository.elasticsearch.user;

import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ICustomUserElasticRepo {
    List<UserDocument> findRandomUsers(int limit, List<UUID> exceptIds);
    Set<UserDocument> searchByKeyword(String keyword, Pageable pageable);
}
