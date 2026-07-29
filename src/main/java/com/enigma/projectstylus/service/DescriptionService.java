package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.DescriptionDTO;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.service.redis.RedisDescriptionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DescriptionService {
    private RedisDescriptionService redisDescriptionService;

    public DescriptionService(RedisDescriptionService redisDescriptionService) {
        this.redisDescriptionService = redisDescriptionService;
    }

    public void addDescription(String roomId, Description description) {
        redisDescriptionService.addDescription(roomId, description);
    }

    public List<DescriptionDTO> getAllDescriptions(String roomId) {
        return redisDescriptionService.fetchAllDescriptions(roomId).stream()
                .map(description -> DescriptionDTO.builder()
                        .id(description.getId())
                        .description(description.getDescription())
                        .playerId(description.getPlayerId())
                        .playerUsername(description.getPlayerUsername())
                        .build())
                .toList();
    }
}
