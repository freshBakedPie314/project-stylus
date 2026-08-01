package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.GuessDTO;
import com.enigma.projectstylus.dto.GuessResponseDTO;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import com.enigma.projectstylus.service.redis.RedisDescriptionService;
import com.enigma.projectstylus.service.redis.RedisRoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuessService {

    private final RedisDescriptionService redisDescriptionService;
    private final RedisRoomService redisRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;

    public GuessService(RedisDescriptionService redisDescriptionService, RedisRoomService redisRoomService,
                        SimpMessagingTemplate simpMessagingTemplate, RoomService roomService) {
        this.redisDescriptionService = redisDescriptionService;
        this.redisRoomService = redisRoomService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomService = roomService;
    }

    public GuessResponseDTO validateGuess(String roomId, GuessDTO guess) {
        // Fetch all descriptions safely
        List<Description> descriptionsInRoom = redisDescriptionService.fetchAllDescriptions(roomId);

        // Find the description safely
        Optional<Description> descriptionOpt = descriptionsInRoom.stream()
                .filter(description -> description.getId().equals(guess.getDescriptionId())) // FIX: Used .equals()
                .findFirst();

        if (descriptionOpt.isEmpty()) {
            return GuessResponseDTO.builder().correct(false).build();
        }

        Description descriptionToCheck = descriptionOpt.get();

        // Check if the guess is right
        if (descriptionToCheck.getMovieId().equals(guess.getGuessedMovieId())) {
            GameRoom room = redisRoomService.getRoom(roomId);

            if (room != null && room.getPlayers() != null) {
                room.getPlayers().forEach(player -> {
                    if (player.getId().equals(guess.getPlayerId())) {
                        long currentGuessed = (player.getTotalGuessed() != null) ? player.getTotalGuessed() : 0;
                        player.setScore(player.getScore() + 100);
                        player.setTotalGuessed(currentGuessed + 1);

                        if (player.getTotalGuessed() == room.getPlayers().size() - 1) {
                            long currentDone = (room.getTotalDone() != null) ? room.getTotalDone() : 0L;
                            room.setTotalDone(currentDone + 1L);
                        }
                    } else if (player.getId().equals(descriptionToCheck.getPlayerId())) {
                        player.setScore(player.getScore() + 50);
                    }
                });

                redisRoomService.saveRoom(room);

                // Broadcast updated room scores publicly to update the leaderboard live
                simpMessagingTemplate.convertAndSend("/topic/" + roomId, room);
            }

            if (room.getTotalDone() == room.getPlayers().size()) {
                roomService.endGame(roomId);
            }
            return GuessResponseDTO.builder()
                    .correct(true)
                    .descriptionId(descriptionToCheck.getId())
                    .movieDescription(descriptionToCheck.getDescription())
                    .movieName(descriptionToCheck.getMovieName())
                    .build();
        } else {
            GameRoom room = redisRoomService.getRoom(roomId);
            room.getPlayers().forEach(player -> {
                if (player.getId().equals(guess.getPlayerId())) {
                    player.setScore(player.getScore() - 25);
                }
            });

            redisRoomService.saveRoom(room);
            simpMessagingTemplate.convertAndSend("/topic/" + roomId, room);
            return GuessResponseDTO.builder()
                    .correct(false)
                    .descriptionId(descriptionToCheck.getId())
                    .build();
        }
    }
}