package com.linkedin.searchservice.service;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocuments;
import com.linkedin.searchservice.repository.PostSearchRepository;
import com.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * consume 3 events
 * 1.when user.created
 * 2.when user.updates profile
 * 3.when post.created
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(
            @Payload Map<String,Object> payload
    ){
        try{
            log.info("Indexing new user: {}",payload.get("userId"));
            UserDocuments documents = new UserDocuments();
            documents.setId((String)payload.get("userId"));
            documents.setFirstName((String)payload.get("firstName"));
            documents.setLastName((String)payload.get("lastName"));
            documents.setEmail((String)payload.get("email"));
            documents.setHeadline((String)payload.get("headline"));
            documents.setLocation((String)payload.get("location"));

            userSearchRepository.save(documents);

            log.info("User indexed: {}",payload.get("userId"));
        } catch (Exception e) {
            log.error("Error indexing user: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdated(
            @Payload Map<String,Object> payload
    ){
        try{
            String userId = payload.get("userId").toString();
            log.info("Updating user index: {}",userId);

            userSearchRepository.findById(userId).ifPresent(doc->{
                doc.setFirstName((String)payload.get("firstName"));
                doc.setLastName((String)payload.get("lastName"));
                doc.setHeadline((String)payload.get("headline"));
                doc.setLocation((String)payload.get("location"));

                if(payload.get("skills") != null){
                    doc.setSkills((List<String>)payload.get("skills"));
                }

                userSearchRepository.save(doc);
                log.info("User index updated: {}",userId);
            });

        }catch (Exception e){
            log.error("error updating user Index: {}",e.getMessage());

        }
    }

    @KafkaListener(topics = "post.created")
    public void consumePostCreated(
            @Payload Map<String,Object> payload
    ){
        try{

            PostDocument document = new PostDocument();
            document.setId((String)payload.get("postId"));
            document.setContent((String)payload.get("content"));
            document.setAuthorId((String)payload.get("authorId"));
            document.setImageUrl((String)payload.get("imageUrl"));
            document.setCreatedAt((String)payload.get("createdAt"));

            postSearchRepository.save(document);

        }catch (Exception e){

            log.error("Error Indexing post: {}",e.getMessage());
        }
    }
}
