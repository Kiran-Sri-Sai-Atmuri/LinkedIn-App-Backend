package com.linkedin.searchservice.controller;

import com.linkedin.searchservice.model.PostDocument;
import com.linkedin.searchservice.model.UserDocuments;
import com.linkedin.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * Search people by name, location ..
     */

    @GetMapping("/people")
    public ResponseEntity<List<UserDocuments>> searchPeople(
            @RequestParam String query
    ){
        return ResponseEntity.ok(searchService.searchPeople(query));
    }

    /**
     * search people by skill
     */
    @GetMapping("/skill")
    public ResponseEntity<List<UserDocuments>> searchBySkill(
            @RequestParam String skill
    ) {
        return ResponseEntity.ok(searchService.searchBySkill(skill));
    }

    /**
     * Search by content
     * @param query
     * @return
     */

    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(
            @RequestParam String query
    ){
        return ResponseEntity.ok(searchService.searchPosts(query));
    }
}
