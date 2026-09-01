package com.linkedin.postservice.repository;

import com.linkedin.postservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {
    List<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId);
}
