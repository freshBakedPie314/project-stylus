package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.DescriptionDTO;
import com.enigma.projectstylus.dto.GiveUpDTO;
import com.enigma.projectstylus.dto.GiveUpNotification;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.redis.RedisDescriptionService;
import com.enigma.projectstylus.service.redis.RedisRoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GiveUpService {

    private final RedisRoomService redisRoomService;
    private final RedisDescriptionService redisDescriptionService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;

    GiveUpService(RedisRoomService redisRoomService, RedisDescriptionService redisDescriptionService, SimpMessagingTemplate simpMessagingTemplate, RoomService roomService) {
        this.redisRoomService = redisRoomService;
        this.redisDescriptionService = redisDescriptionService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomService = roomService;
    }

    public List<Description> giveUp(String roomId, GiveUpDTO giveUpDTO) {
        // Get room and player
        GameRoom room = redisRoomService.getRoom(roomId);
        Player giveUpPlayer = room.getPlayers().stream().filter(
                player -> player.getId().equals(giveUpDTO.getUserId())
        ).findFirst().orElse(null);

        // Update room done value
        long currentDone = (room.getTotalDone() != null) ? room.getTotalDone() : 0L;
        room.setTotalDone(currentDone + 1L);
        redisRoomService.saveRoom(room);

        // Broadcast message
        if (giveUpPlayer != null) {
            simpMessagingTemplate.convertAndSend("/topic/" + roomId,
                    GiveUpNotification.builder()
                            .userId(giveUpPlayer.getId())
                            .username(giveUpPlayer.getUsername())
                            .build()
            );
        }
        else return List.of();

        // Fetch all movies
        List<Description> descriptionDTOList = redisDescriptionService.fetchAllDescriptions(roomId);

        // Filter and send
        descriptionDTOList.removeIf(
                description -> description.getPlayerId().equals(giveUpPlayer.getId())
        );

        // Check if this change updates game
        if(room.getTotalDone() != null && room.getTotalDone() == room.getPlayers().size())
        {
            roomService.endGame(roomId);
        }
        return descriptionDTOList;
    }
}
