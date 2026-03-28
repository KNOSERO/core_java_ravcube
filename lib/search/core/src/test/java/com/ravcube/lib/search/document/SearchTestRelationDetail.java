package com.ravcube.lib.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class SearchTestRelationDetail {

    @Field(type = FieldType.Keyword)
    private String key;

    @Field(type = FieldType.Text)
    private String value;

    public SearchTestRelationDetail() {
    }

    public SearchTestRelationDetail(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
