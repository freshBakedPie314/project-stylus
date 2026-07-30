package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.GiveUpDTO;
import com.enigma.projectstylus.dto.GuessDTO;
import com.enigma.projectstylus.dto.GuessResponseDTO;
import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.DescriptionService;
import com.enigma.projectstylus.service.GiveUpService;
import com.enigma.projectstylus.service.GuessService;
import com.enigma.projectstylus.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RoomWSController {

    private final RoomService roomService;
    private final DescriptionService descriptionService;
    private final GuessService guessService;
    private final GiveUpService giveUpService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public RoomWSController(RoomService roomService, DescriptionService descriptionService, GuessService guessService, GiveUpService giveUpService, SimpMessagingTemplate simpMessagingTemplate) {
        this.roomService = roomService;
        this.descriptionService = descriptionService;
        this.guessService = guessService;
        this.giveUpService = giveUpService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/room.join/{roomId}")
    public void joinRoom(@DestinationVariable String roomId, @Payload Player player) {
        roomService.joinRoom(roomId, player);
    }

    @MessageMapping("/room.start/{roomId}")
    public void startRoom(@DestinationVariable String roomId) {
        roomService.startGame(roomId);
    }

    @MessageMapping("/room.submit/{roomId}")
    public void submitDescription(@DestinationVariable String roomId, @Payload Description description) {
        descriptionService.addDescription(roomId, description);

        roomService.handlePlayerSubmission(roomId, description);
    }

    @MessageMapping("/room.guess/{roomId}")
    public void switchStateTOGuessing(@DestinationVariable String roomId) {
        roomService.startGuess(roomId);
    }

    @MessageMapping("/room.guess-submit/{roomId}")
    public void validateGuess(@DestinationVariable String roomId, @Payload GuessDTO guess) {
        GuessResponseDTO response = guessService.validateGuess(roomId, guess);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId + "/user/" + guess.getPlayerId(),  response);
    }

    @MessageMapping("/room.give-up/{roomId}")
    public void giveUp(@DestinationVariable String roomId, @Payload GiveUpDTO giveUpDTO) {
        List<Description> revealedAnswers = giveUpService.giveUp(roomId, giveUpDTO);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId + "/user/" + giveUpDTO.getUserId(), revealedAnswers);
    }
}
