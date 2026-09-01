package com.linkedin.feedservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FEED_KEY_PREFIX = "feed:";
    public List<String> getFeed(String userId, int page, int size) {
        log.info("Getting feed for user: {}",userId);
        String feedKey = FEED_KEY_PREFIX+userId;
        int start = page * size;
        int end = start + size -1;

        List<Object> postIds = redisTemplate.opsForList()
                .range(feedKey,start,end);

        if(postIds == null || postIds.isEmpty()){
            log.info("Feed cache empty for user: {}",userId);
            return new ArrayList<>();
        }

        List<String> result = postIds.stream()
                .map(Object::toString)
                .toList();

        log.info("Returning {} posts for user: {} ",result.size(),userId);

        return result;
    }


    /**
     * clear cache for user
     * @param userId
     */
    public void clearFeed(String userId) {
        String feedKey = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(feedKey);

        log.info("Feed cache cleared for user: {}",userId);
    }
}
