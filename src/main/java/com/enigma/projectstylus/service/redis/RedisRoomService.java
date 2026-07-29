package com.enigma.projectstylus.service.redis;

import com.enigma.projectstylus.model.GameRoom;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisRoomService {
    private RedisTemplate<String, GameRoom> gameRoomRedisTemplate;
    private static final String KEY_PREFIX = "room:";

    RedisRoomService(RedisTemplate<String, GameRoom> gameRoomRedisTemplate) {
        this.gameRoomRedisTemplate = gameRoomRedisTemplate;
    }

    public void saveRoom(GameRoom gameRoom)
    {
        String key = KEY_PREFIX + gameRoom.getRoomId();
        gameRoomRedisTemplate.opsForValue().set(key, gameRoom, 2, TimeUnit.HOURS);
    }

    public GameRoom getRoom(String roomId)
    {
        String key = KEY_PREFIX + roomId;
        return gameRoomRedisTemplate.opsForValue().get(key);
    }

    public void deleteRoom(String roomId)
    {
        String key = KEY_PREFIX + roomId;
        gameRoomRedisTemplate.delete(key);
    }
}
