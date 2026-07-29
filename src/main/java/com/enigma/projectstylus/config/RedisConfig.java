package com.enigma.projectstylus.config;

import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, GameRoom> gameRoomRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, GameRoom> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());

        JacksonJsonRedisSerializer jacksonJsonRedisSerializer = new JacksonJsonRedisSerializer(GameRoom.class);
        redisTemplate.setValueSerializer(jacksonJsonRedisSerializer);

        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Description> descriptionRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Description> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        JacksonJsonRedisSerializer jacksonJsonRedisSerializer = new JacksonJsonRedisSerializer(Description.class);
        redisTemplate.setValueSerializer(jacksonJsonRedisSerializer);
        return redisTemplate;
    }
}
