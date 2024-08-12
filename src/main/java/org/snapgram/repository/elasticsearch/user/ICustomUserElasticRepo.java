package org.snapgram.repository.elasticsearch.user;

import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ICustomUserElasticRepo {
    Set<UserDocument> searchByKeyword(String keyword, Pageable pageable);
}
