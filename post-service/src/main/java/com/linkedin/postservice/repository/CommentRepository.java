package com.linkedin.postservice.repository;

import com.linkedin.postservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,String> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);
}
