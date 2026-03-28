package com.ravcube.lib.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class SearchTestRelation {

    @Field(type = FieldType.Keyword)
    private String key;

    @Field(type = FieldType.Text)
    private String value;

    @Field(type = FieldType.Nested)
    private List<SearchTestRelationDetail> details;

    public SearchTestRelation() {
        this.details = List.of();
    }

    public SearchTestRelation(String key, String value) {
        this(key, value, List.of());
    }

    public SearchTestRelation(String key, String value, List<SearchTestRelationDetail> details) {
        this.key = key;
        this.value = value;
        this.details = List.copyOf(details);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public List<SearchTestRelationDetail> getDetails() {
        return details;
    }
}
