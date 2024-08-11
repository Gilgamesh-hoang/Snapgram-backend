package org.snapgram.repository.elasticsearch.user;

import lombok.RequiredArgsConstructor;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CustomUserElasticRepo implements ICustomUserElasticRepo {
    @NonNull
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<UserDocument> searchByKeyword(String keyword, Pageable pageable) {
        keyword = keyword.toLowerCase();
        String queryString = String.format(
                "{ \"bool\": { \"should\": [ " +
                        "{ \"wildcard\": { \"nickname\": { \"value\": \"*%s*\" } } }, " +
                        "{ \"wildcard\": { \"fullName\": { \"value\": \"*%s*\" } } } " +
                        "] } }",
                keyword, keyword);

        // Create a StringQuery with the formatted query string
        Query query = new StringQuery(queryString, pageable);
        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();
    }
}
