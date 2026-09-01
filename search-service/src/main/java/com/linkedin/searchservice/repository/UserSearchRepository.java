package com.linkedin.searchservice.repository;

import com.linkedin.searchservice.model.UserDocuments;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocuments,String> {
    @Query("{\"multi_match\":{\"query\":\"?0\","+"\"fields\":[\"firstName\",\"lastName\","+"\"headline\",\"location\"]}}")
    List<UserDocuments> searchUsers(String query);

    List<UserDocuments> findBySkillsContaining(String skill);
}
