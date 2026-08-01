package com.enigma.projectstylus.service;

import com.enigma.projectstylus.RoomStatus;
import com.enigma.projectstylus.dto.DescriptionDTO;
import com.enigma.projectstylus.dto.GuessingPhaseInitPayload;
import com.enigma.projectstylus.dto.LeaderboardPhasePayload;
import com.enigma.projectstylus.dto.RoomCreatioonPayload;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.redis.RedisDescriptionService;
import com.enigma.projectstylus.service.redis.RedisRoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RoomService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RedisRoomService redisRoomService;
    private final RedisDescriptionService redisDescriptionService;
    private final DescriptionService descriptionService;
    private final ScheduledExecutorService scheduler;

    RoomService(SimpMessagingTemplate simpMessagingTemplate, RedisRoomService redisRoomService,
                RedisDescriptionService redisDescriptionService, DescriptionService descriptionService,
                ScheduledExecutorService scheduler) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.redisRoomService = redisRoomService;
        this.redisDescriptionService = redisDescriptionService;
        this.descriptionService = descriptionService;
        this.scheduler = scheduler;
    }

    public String createRoom(RoomCreatioonPayload roomCreatioonPayload)
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
                        .totalDone(0L)
                        .writingLimit(roomCreatioonPayload.getWritingTime())
                        .guessingLimit(roomCreatioonPayload.getGuessingTime())
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

            // Calculate end time of Writing phase
            long endTime = System.currentTimeMillis() + (room.getWritingLimit() * 1000L);
            room.setPhaseEndTime(endTime);

            redisRoomService.saveRoom(room);

            simpMessagingTemplate.convertAndSend("/topic/" + roomId, room);
            scheduler.schedule(() -> forceTransitionToGuessing(roomId), room.getWritingLimit(), TimeUnit.SECONDS);
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

        long endTime = System.currentTimeMillis() + (room.getGuessingLimit() * 1000L);
        room.setPhaseEndTime(endTime);

        redisRoomService.saveRoom(room);

        // Wrap the immutable list in a mutable ArrayList so it can be shuffled smoothly
        List<DescriptionDTO> randomizedDescriptions = new ArrayList<>(descriptionService.getAllDescriptions(roomId));
        Collections.shuffle(randomizedDescriptions);

        GuessingPhaseInitPayload payload = new GuessingPhaseInitPayload(room, randomizedDescriptions);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, payload);

        scheduler.schedule(() -> endGame(roomId), room.getGuessingLimit(), TimeUnit.SECONDS);
    }

    public void endGame(String roomId) {
        GameRoom room = redisRoomService.getRoom(roomId);
        if (room == null) return;

        room.setStatus(RoomStatus.LEADERBOARD);
        redisRoomService.saveRoom(room);

        List<Description> descriptions = redisDescriptionService.fetchAllDescriptions(roomId);
        LeaderboardPhasePayload payload = new LeaderboardPhasePayload(room, descriptions);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, payload);
    }

    public void resetRoom(String roomId) {

        synchronized (roomId.intern())
        {
            GameRoom room = redisRoomService.getRoom(roomId);
            if (room == null) {
                throw new IllegalArgumentException("Room not found");
            }

            room.setStatus(RoomStatus.LOBBY);
            room.setTotalDone(0L);

            redisDescriptionService.clearDescription(roomId);

            if (room.getPlayers() != null) {
                for (Player player : room.getPlayers()) {
                    player.setScore(0L);
                    player.setHasSubmitted(false);
                    player.setTotalGuessed(0L);
                }
            }

            redisRoomService.saveRoom(room);
            simpMessagingTemplate.convertAndSend("/topic/" + roomId, room);
            Map<String, Object> logMessage = Map.of(
                    "user", "System",
                    "message", "The match has been reset by the host. Welcome back to the lobby!"
            );
            simpMessagingTemplate.convertAndSend("/topic/" + roomId, (Object) logMessage);
        }
    }

    private void forceTransitionToGuessing(String roomId) {
        GameRoom room = redisRoomService.getRoom(roomId);
        if (room == null || room.getStatus() != RoomStatus.WRITING) return;

        boolean penalizeTriggered = false;

        if (room.getPlayers() != null) {
            // Step 1: Find the slackers and apply the penalty logic
            for (Player p : room.getPlayers()) {
                if (p.getHasSubmitted() == null || !p.getHasSubmitted()) {
                    penalizeTriggered = true;

                    // Dock points (Allow scores to go negative for maximum public shame!)
                    long currentScore = p.getScore() != null ? p.getScore() : 0L;
                    p.setScore(currentScore - 250L);

                    Description dummyDesc = new Description();
                    dummyDesc.setId(UUID.randomUUID());
                    dummyDesc.setMovieId(-1L);
                    dummyDesc.setMovieName("A Mystery Film");
                    dummyDesc.setPlayerId(p.getId());
                    dummyDesc.setPlayerUsername(p.getUsername());
                    dummyDesc.setDescription("SHAME! This player spent the entire phase zoning out and wrote absolutely nothing!");

                    descriptionService.addDescription(roomId, dummyDesc);
                    p.setHasSubmitted(true);

                    // Send a systemic public call-out to the chat/logs channel
                    Map<String, Object> logMessage = Map.of(
                            "user", "SYSTEM",
                            "message", "PSA: " + p.getUsername() + " failed to submit in time! Penalty applied: -250 points!"
                    );
                    simpMessagingTemplate.convertAndSend("/topic/" + roomId, (Object) logMessage);
                }
            }

            // Step 2: Reward the players who actually submitted on time
            if (penalizeTriggered) {
                for (Player p : room.getPlayers()) {
                    // If they weren't the ones docked and actually completed the task
                    if (p.getScore() != null && p.getScore() >= 0) {
                        long currentScore = p.getScore() != null ? p.getScore() : 0L;
                        p.setScore(currentScore + 100L); // Punctuality bonus
                    }
                }
            }

            redisRoomService.saveRoom(room);
        }

        // Advance to guessing phase
        startGuess(roomId);
    }
}
