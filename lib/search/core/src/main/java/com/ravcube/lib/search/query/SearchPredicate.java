package com.ravcube.lib.search.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.Objects;

public final class SearchPredicate {

    private final Query query;

    private SearchPredicate(Query query) {
        this.query = Objects.requireNonNull(query, "query must not be null");
    }

    public static SearchPredicate of(Query query) {
        return new SearchPredicate(query);
    }

    public Query toQuery() {
        return query;
    }

    public SearchPredicate must(SearchPredicate other) {
        SearchPredicate right = Objects.requireNonNull(other, "other must not be null");
        return new SearchPredicate(Query.of(builder -> builder.bool(bool -> bool.must(query).must(right.query))));
    }

    public SearchPredicate should(SearchPredicate other) {
        SearchPredicate right = Objects.requireNonNull(other, "other must not be null");
        return new SearchPredicate(Query.of(builder -> builder.bool(bool -> bool.should(query).should(right.query))));
    }

    public SearchPredicate mustNot(SearchPredicate other) {
        SearchPredicate right = Objects.requireNonNull(other, "other must not be null");
        return new SearchPredicate(Query.of(builder -> builder.bool(bool -> bool.must(query).mustNot(right.query))));
    }

    public SearchPredicate not() {
        return new SearchPredicate(Query.of(builder -> builder.bool(bool -> bool.mustNot(query))));
    }
}
