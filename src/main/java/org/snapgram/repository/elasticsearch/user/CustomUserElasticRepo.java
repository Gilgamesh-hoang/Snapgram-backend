package org.snapgram.repository.elasticsearch.user;

import co.elastic.clients.elasticsearch._types.query_dsl.RandomScoreFunction;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class CustomUserElasticRepo implements ICustomUserElasticRepo {
    ElasticsearchOperations elasticsearchOperations;

    @Override
    public Set<UserDocument> findRandomUsers(int limit, List<UUID> exceptIds) {
        List<String> list = exceptIds.stream().map(UUID::toString).toList();
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .functionScore(fs -> fs
                                .query(fq -> fq
                                        .bool(b -> b
                                                .must(m -> m.term(t -> t.field("isDeleted").value(false)))
                                                .must(m -> m.term(t -> t.field("isActive").value(true)))
                                                .mustNot(m -> m.ids(i -> i.values(list)))
                                        )
                                )
                                .functions(f -> f.randomScore(new RandomScoreFunction.Builder().build()))
                        )
                )
                .withPageable(PageRequest.of(0, limit))
                .build();
        return executeSearch(query);
    }

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

        return executeSearch(query);
    }

    @Override
    public Set<UserDocument> searchFollow(String keyword, List<UUID> followIds, Pageable pageable) {
        List<String> ids = followIds.stream().map(UUID::toString).toList();
        keyword = keyword.toLowerCase();
        String finalKeyword = keyword;

        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m.term(t -> t.field("isDeleted").value(false)))
                                .must(m -> m.term(t -> t.field("isActive").value(true)))
                                .must(m -> m.ids(i -> i.values(ids)))
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
        return executeSearch(query);
    }

    private Set<UserDocument> executeSearch(Query query) {
        SearchHits<UserDocument> searchHits = elasticsearchOperations.search(query, UserDocument.class);
        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toSet());
    }
}