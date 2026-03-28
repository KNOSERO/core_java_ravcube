package com.ravcube.lib.search.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "search-service-test-document")
public class SearchTestDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String code;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Integer)
    private Integer priority;

    @Field(type = FieldType.Nested)
    private List<SearchTestRelation> relations;

    @Field(type = FieldType.Nested)
    private List<SearchTestRelation> labels;

    public SearchTestDocument() {
    }

    public SearchTestDocument(String id, String code, String name) {
        this(id, code, name, 0, List.of(), List.of());
    }

    public SearchTestDocument(String id, String code, String name, Integer priority) {
        this(id, code, name, priority, List.of(), List.of());
    }

    public SearchTestDocument(String id, String code, String name, Integer priority, List<SearchTestRelation> relations) {
        this(id, code, name, priority, relations, List.of());
    }

    public SearchTestDocument(String id, String code, String name, Integer priority,
                              List<SearchTestRelation> relations, List<SearchTestRelation> labels) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.priority = priority;
        this.relations = List.copyOf(relations);
        this.labels = List.copyOf(labels);
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getPriority() {
        return priority;
    }

    public List<SearchTestRelation> getRelations() {
        return relations;
    }

    public List<SearchTestRelation> getLabels() {
        return labels;
    }
}
