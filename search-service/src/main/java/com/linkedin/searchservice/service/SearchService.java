package com.linkedin.searchservice.service;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocuments;
import com.linkedin.searchservice.repository.PostSearchRepository;
import com.linkedin.searchservice.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public List<UserDocuments> searchPeople(String query) {
        log.info("searching users: {}",query);


        return userSearchRepository.searchUsers(query);
    }

    /**
     * Search user by skill
     * @param skill
     * @return
     */
    public List<UserDocuments> searchBySkill(String skill) {
        log.info("Searching users by skill: {}",skill);
        return userSearchRepository.findBySkillsContaining(skill);
    }

    public List<PostDocument> searchPosts(String query) {
        log.info("Searching posts: {}",query);
        return postSearchRepository.searchPosts(query);
    }
}
