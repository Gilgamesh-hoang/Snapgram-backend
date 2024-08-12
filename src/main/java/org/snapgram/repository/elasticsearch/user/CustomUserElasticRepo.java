package org.snapgram.repository.elasticsearch.user;

import lombok.RequiredArgsConstructor;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class CustomUserElasticRepo implements ICustomUserElasticRepo {
    @NonNull
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Set<UserDocument> searchByKeyword(String keyword, Pageable pageable) {
        keyword = keyword.toLowerCase();
        String finalKeyword = keyword;

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.term(t -> t.field("isDeleted").value(false)))
                                .must(m -> m.term(t -> t.field("isActive").value(true)))
                                .must(m -> m
                                        .bool(b1 -> b1
                                                .should(s -> s.wildcard(w -> w.field("nickname").value("*" + finalKeyword + "*")))
                                                .should(s -> s.wildcard(w -> w.field("fullName").value("*" + finalKeyword + "*")))
                                                .should(s -> s.multiMatch(mul -> mul.query(finalKeyword).fields(List.of("fullName", "nickname")).fuzziness("AUTO")))
                                        )
                                )
                        )
                )
                .withPageable(pageable)
                .build();

        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);

        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toSet());
    }
}
