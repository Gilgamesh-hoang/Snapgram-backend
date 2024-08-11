package org.snapgram.repository.elasticsearch.user;

import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICustomUserElasticRepo {
    List<UserDocument> searchByKeyword(String keyword, Pageable pageable);
}
