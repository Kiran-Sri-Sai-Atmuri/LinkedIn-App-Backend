package com.linkedin.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * document for post search
 * Indexes when post created
 * Enables full text search
 */
@Document(indexName = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Keyword)
    private String authorId;

    private String imageUrl;

    private String createdAt;

}
