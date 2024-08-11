package org.snapgram.repository.elasticsearch.user;

import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface UserElasticRepo extends ElasticsearchRepository<UserDocument, UUID> {

}