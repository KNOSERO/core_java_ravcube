package com.ravcube.lib.search.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SearchQuery {

    public ScopedSearchQuery in(String path) {
        return new ScopedSearchQuery(normalizePath(path));
    }

    public SearchPredicate matchAll() {
        return SearchPredicate.of(Query.of(query -> query.matchAll(matchAll -> matchAll)));
    }

    public SearchPredicate term(String field, String value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryValue = Objects.requireNonNull(value, "value must not be null");
        return SearchPredicate.of(Query.of(query -> query.term(term -> term
                .field(queryField)
                .value(FieldValue.of(queryValue)))));
    }

    public SearchPredicate terms(String field, String... values) {
        return terms(field, Arrays.asList(values));
    }

    public SearchPredicate terms(String field, Collection<String> values) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        Collection<String> queryValues = Objects.requireNonNull(values, "values must not be null");
        List<FieldValue> termValues = queryValues.stream()
                .map(value -> FieldValue.of(Objects.requireNonNull(value, "value must not be null")))
                .toList();
        if (termValues.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        return SearchPredicate.of(Query.of(query -> query.terms(terms -> terms
                .field(queryField)
                .terms(termsField -> termsField.value(termValues)))));
    }

    public SearchPredicate match(String field, String value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryValue = Objects.requireNonNull(value, "value must not be null");
        return SearchPredicate.of(Query.of(query -> query.match(match -> match
                .field(queryField)
                .query(FieldValue.of(queryValue)))));
    }

    public SearchPredicate matchPhrase(String field, String phrase) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryPhrase = Objects.requireNonNull(phrase, "phrase must not be null");
        return SearchPredicate.of(Query.of(query -> query.matchPhrase(matchPhrase -> matchPhrase
                .field(queryField)
                .query(queryPhrase))));
    }

    public SearchPredicate prefix(String field, String prefix) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryPrefix = Objects.requireNonNull(prefix, "prefix must not be null");
        return SearchPredicate.of(Query.of(query -> query.prefix(prefixQuery -> prefixQuery
                .field(queryField)
                .value(queryPrefix)
                .caseInsensitive(true))));
    }

    public SearchPredicate wildcard(String field, String pattern) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryPattern = Objects.requireNonNull(pattern, "pattern must not be null");
        return SearchPredicate.of(Query.of(query -> query.wildcard(wildcard -> wildcard
                .field(queryField)
                .value(queryPattern)
                .caseInsensitive(true))));
    }

    public SearchPredicate exists(String field) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.exists(exists -> exists.field(queryField))));
    }

    public SearchPredicate notExists(String field) {
        return exists(field).not();
    }

    public SearchPredicate gt(String field, double value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.range(range -> range
                .number(number -> number.field(queryField).gt(value)))));
    }

    public SearchPredicate gte(String field, double value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.range(range -> range
                .number(number -> number.field(queryField).gte(value)))));
    }

    public SearchPredicate lt(String field, double value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.range(range -> range
                .number(number -> number.field(queryField).lt(value)))));
    }

    public SearchPredicate lte(String field, double value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.range(range -> range
                .number(number -> number.field(queryField).lte(value)))));
    }

    public SearchPredicate between(String field, double fromInclusive, double toInclusive) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        return SearchPredicate.of(Query.of(query -> query.range(range -> range
                .number(number -> number.field(queryField).gte(fromInclusive).lte(toInclusive)))));
    }

    public SearchPredicate fuzzy(String field, String value) {
        String queryField = Objects.requireNonNull(field, "field must not be null");
        String queryValue = Objects.requireNonNull(value, "value must not be null");
        return SearchPredicate.of(Query.of(query -> query.fuzzy(fuzzy -> fuzzy
                .field(queryField)
                .value(queryValue))));
    }

    public SearchPredicate queryString(String queryExpression) {
        String expression = Objects.requireNonNull(queryExpression, "queryExpression must not be null");
        return SearchPredicate.of(Query.of(query -> query.queryString(queryString -> queryString.query(expression))));
    }

    public SearchPredicate nestedTerm(String path, String field, String value) {
        return in(path).term(field, value);
    }

    public SearchPredicate nestedMatch(String path, String field, String value) {
        return in(path).match(field, value);
    }

    private String nestedField(String path, String field) {
        String nestedField = Objects.requireNonNull(field, "field must not be null");
        return path + "." + nestedField;
    }

    private String normalizePath(String path) {
        String nestedPath = Objects.requireNonNull(path, "path must not be null").trim();
        if (nestedPath.isEmpty()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return nestedPath;
    }

    public final class ScopedSearchQuery {
        private final String path;

        private ScopedSearchQuery(String path) {
            this.path = path;
        }

        public ScopedSearchQuery in(String nestedPath) {
            return new ScopedSearchQuery(path + "." + normalizePath(nestedPath));
        }

        public SearchPredicate term(String field, String value) {
            return wrap(SearchQuery.this.term(nestedField(path, field), value));
        }

        public SearchPredicate terms(String field, String... values) {
            return wrap(SearchQuery.this.terms(nestedField(path, field), values));
        }

        public SearchPredicate terms(String field, Collection<String> values) {
            return wrap(SearchQuery.this.terms(nestedField(path, field), values));
        }

        public SearchPredicate match(String field, String value) {
            return wrap(SearchQuery.this.match(nestedField(path, field), value));
        }

        public SearchPredicate matchPhrase(String field, String phrase) {
            return wrap(SearchQuery.this.matchPhrase(nestedField(path, field), phrase));
        }

        public SearchPredicate prefix(String field, String prefix) {
            return wrap(SearchQuery.this.prefix(nestedField(path, field), prefix));
        }

        public SearchPredicate wildcard(String field, String pattern) {
            return wrap(SearchQuery.this.wildcard(nestedField(path, field), pattern));
        }

        public SearchPredicate exists(String field) {
            return wrap(SearchQuery.this.exists(nestedField(path, field)));
        }

        public SearchPredicate notExists(String field) {
            return wrap(SearchQuery.this.notExists(nestedField(path, field)));
        }

        public SearchPredicate gt(String field, double value) {
            return wrap(SearchQuery.this.gt(nestedField(path, field), value));
        }

        public SearchPredicate gte(String field, double value) {
            return wrap(SearchQuery.this.gte(nestedField(path, field), value));
        }

        public SearchPredicate lt(String field, double value) {
            return wrap(SearchQuery.this.lt(nestedField(path, field), value));
        }

        public SearchPredicate lte(String field, double value) {
            return wrap(SearchQuery.this.lte(nestedField(path, field), value));
        }

        public SearchPredicate between(String field, double fromInclusive, double toInclusive) {
            return wrap(SearchQuery.this.between(nestedField(path, field), fromInclusive, toInclusive));
        }

        public SearchPredicate fuzzy(String field, String value) {
            return wrap(SearchQuery.this.fuzzy(nestedField(path, field), value));
        }

        private SearchPredicate wrap(SearchPredicate predicate) {
            SearchPredicate scopedPredicate = Objects.requireNonNull(predicate, "predicate must not be null");
            return SearchPredicate.of(Query.of(query -> query.nested(nested -> nested
                    .path(path)
                    .query(scopedPredicate.toQuery()))));
        }
    }
}
