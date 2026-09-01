package com.linkedin.searchservice.repository;

import com.linkedin.searchservice.model.PostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument,String> {
    @Query("{\"match\":{\"content\":{\"query\": \"?0\","+"\"fizziness\":\"AUTO\"}}}")
    List<PostDocument> searchPosts(String query);
}
