package com.linkedin.postservice.service;

import com.linkedin.postservice.entity.Comment;
import com.linkedin.postservice.entity.Like;
import com.linkedin.postservice.entity.Post;
import com.linkedin.postservice.repository.CommentRepository;
import com.linkedin.postservice.repository.LikeRepository;
import com.linkedin.postservice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final String POST_CREATED_TOPIC = "post.created";
    private static final String POST_LIKED_TOPIC = "post.liked";
    private static final String POST_COMMENTED_TOPIC = "post.commented";

    /**
     * Create post
     */

    public Post createPost(String authorId, String content, MultipartFile image) {

        log.info("creating post for user: {}",authorId);
        String imageUrl = null;
        if(image!=null && !image.isEmpty()){
            imageUrl = s3Service.uploadFile(
                    image,"posts/"+authorId
            );
        }


        Post post = Post.builder()
                .authorId(authorId)
                .content(content)
                .imageUrl(imageUrl)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post created: {}",savedPost.getId());

        // publish event to kafka

        Map<String,Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("postId",savedPost.getId());
        postCreatedEvent.put("authorId",savedPost.getAuthorId());
        postCreatedEvent.put("content",savedPost.getContent());
        postCreatedEvent.put("imageUrl",savedPost.getImageUrl());
        postCreatedEvent.put("createdAt",savedPost.getCreatedAt().toString());

        kafkaTemplate.send(POST_CREATED_TOPIC,savedPost.getId(),postCreatedEvent);

        log.info("post.created event published : {}",savedPost.getId());

        return savedPost;
    }


    public Post getPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException(
                        "Post not found: "+postId
                ));
    }


    public List<Post> getUserPosts(String userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }


    /**
     * like or unlike a post
     * @param postId
     * @param userId
     * @return
     */
    public String likePost(String postId, String userId) {
        Post post = getPost(postId);

        if(likeRepository.existsByPostIdAndUserId(postId,userId)){
            likeRepository.findByPostIdAndUserId(postId,userId)
                    .ifPresent(likeRepository::delete);
            post.setLikeCount(post.getLikeCount() - 1);
            return "Post unliked";
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();

        likeRepository.save(like);

        post.setLikeCount(post.getLikeCount()+1);

        postRepository.save(post);

        //Publish post.liked event
        Map<String,Object> postLikedEvent = new HashMap<>();
        postLikedEvent.put("postId",postId);
        postLikedEvent.put("userId",userId);
        postLikedEvent.put("authorId",post.getAuthorId());

        kafkaTemplate.send(POST_LIKED_TOPIC,postId,postLikedEvent);

        return "Post liked";
    }

    public Comment addComment(String postId, String authorId, String content) {

        Post post = getPost(postId);

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount( post.getCommentCount() + 1);
        postRepository.save(post);

        // Publish post.commented event

        Map<String,Object> postCommentedEvent = new HashMap<>();
        postCommentedEvent.put("postId",postId);
        postCommentedEvent.put("authorId",authorId);
        postCommentedEvent.put("commentId",savedComment.getId());
        postCommentedEvent.put("postAuthorId",post.getAuthorId());


        kafkaTemplate.send(POST_COMMENTED_TOPIC,postId,postCommentedEvent);

        return savedComment;
    }

    public List<Comment> getComments(String postId) {

        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public void deletePost(String postId, String userId) {
        Post post = getPost(postId);

        if(!post.getAuthorId().equals(userId)){
            throw new RuntimeException("NOt authorized to delete this post");
        }
        postRepository.delete(post);
        log.info("Post deleted : {}",postId);
    }
}
