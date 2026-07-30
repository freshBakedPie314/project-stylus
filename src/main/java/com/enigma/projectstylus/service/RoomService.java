package com.enigma.projectstylus.service;

import com.enigma.projectstylus.RoomStatus;
import com.enigma.projectstylus.dto.DescriptionDTO;
import com.enigma.projectstylus.dto.GuessingPhaseInitPayload;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.redis.RedisDescriptionService;
import com.enigma.projectstylus.service.redis.RedisRoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RedisRoomService redisRoomService;
    private final RedisDescriptionService redisDescriptionService;
    private final DescriptionService descriptionService;

    RoomService(SimpMessagingTemplate simpMessagingTemplate, RedisRoomService redisRoomService,
                RedisDescriptionService redisDescriptionService, DescriptionService descriptionService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.redisRoomService = redisRoomService;
        this.redisDescriptionService = redisDescriptionService;
        this.descriptionService = descriptionService;
    }

    public String createRoom()
    {
        String roomId = UUID.randomUUID().toString().substring(0, 5);
        simpMessagingTemplate.convertAndSend("/topic/room.system", "Room created with ID: " + roomId);

        //Handle storing in redis
        redisRoomService.saveRoom(
                GameRoom.builder()
                        .roomId(roomId)
                        .time(60L)
                        .status(RoomStatus.LOBBY)
                        .players(new ArrayList<>())
                        .build()
        );

        return roomId;
    }

    public void joinRoom (String roomId, Player player)
    {
        // Update the room
        GameRoom room = redisRoomService.getRoom(roomId);

        if (room != null) {
            List<Player> activePlayers = room.getPlayers() != null
                    ? new ArrayList<>(room.getPlayers())
                    : new ArrayList<>();

            activePlayers.add(player);
            room.setPlayers(activePlayers);

            redisRoomService.saveRoom(room);

            simpMessagingTemplate.convertAndSend("/topic/" + roomId, player.getUsername() + " Joined!!");
        }
    }

    public void startGame(String roomId) {
        GameRoom room = redisRoomService.getRoom(roomId);
        if (room != null) {
            room.setStatus(RoomStatus.WRITING);
            redisRoomService.saveRoom(room);

            simpMessagingTemplate.convertAndSend("/topic/" + roomId, room);

        }
    }

    public void handlePlayerSubmission(String roomId, Description description) {
        GameRoom room = redisRoomService.getRoom(roomId);
        if (room == null) return;

        if (room.getPlayers() != null) {
            for (Player p : room.getPlayers()) {
                if (p.getId().equals(description.getPlayerId())) {
                    p.setHasSubmitted(true);
                }
            }
            redisRoomService.saveRoom(room);
        }

        // Add the desc to the list
        List<Description> submissions = redisDescriptionService.fetchAllDescriptions(roomId);
        int totalPlayers = room.getPlayers().size();

        // Send a minor notification frame that a submission was captured
        String notificationMsg = description.getPlayerUsername() + " has submitted their description! (" + submissions.size() + "/" + totalPlayers + ")";
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, notificationMsg);

        if (submissions.size() >= totalPlayers && totalPlayers > 0) {
            startGuess(roomId);
        }
    }

    public void startGuess(String roomId) {
        GameRoom room = redisRoomService.getRoom(roomId);
        if (room == null) return;

        room.setStatus(RoomStatus.GUESSING);
        redisRoomService.saveRoom(room);

        // Wrap the immutable list in a mutable ArrayList so it can be shuffled
        List<DescriptionDTO> randomizedDescriptions = new ArrayList<>(descriptionService.getAllDescriptions(roomId));
        Collections.shuffle(randomizedDescriptions);

        GuessingPhaseInitPayload payload = new GuessingPhaseInitPayload(room, randomizedDescriptions);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, payload);
    }
}
