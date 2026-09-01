package com.linkedin.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Document for user search
 * Indexes when user registers or updates
 * Enables full text search across name,headline,skills,location
 */

@Document(indexName = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDocuments {

    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String firstName;
    @Field(type = FieldType.Text)
    private String lastName;
    @Field(type = FieldType.Text)
    private String headline;
    @Field(type = FieldType.Keyword)
    private String location;
    @Field(type = FieldType.Keyword)
    private List<String> skills;
    @Field(type = FieldType.Keyword)
    private String email;
    private String profilePhotoUrl;

}
