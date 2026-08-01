package com.enigma.projectstylus.service.redis;

import com.enigma.projectstylus.model.Description;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisDescriptionService {
    private RedisTemplate<String, Description> descriptionRedisTemplate;
    public RedisDescriptionService(RedisTemplate<String, Description> descriptionRedisTemplate) {
        this.descriptionRedisTemplate = descriptionRedisTemplate;
    }

    private static final String KEY_PREFIX  = "descriptions:";
    public void addDescription(String roomId, Description description) {
        String key = KEY_PREFIX + roomId;
        descriptionRedisTemplate.opsForList().rightPush(key, description);
        descriptionRedisTemplate.expire(key, 10, TimeUnit.MINUTES);
    }

    public void clearDescription(String roomId) {
        String key = KEY_PREFIX + roomId;
        descriptionRedisTemplate.delete(key);
    }

    public List<Description> fetchAllDescriptions(String roomId) {
        String key = KEY_PREFIX + roomId;
        List<Description> descriptions = descriptionRedisTemplate.opsForList().range(key, 0, -1);
        return descriptions != null ? descriptions : new ArrayList<>();
    }
}
