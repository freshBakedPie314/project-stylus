package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.DescriptionService;
import com.enigma.projectstylus.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class RoomWSController {

    private final RoomService roomService;
    private final DescriptionService descriptionService;

    public RoomWSController(RoomService roomService, DescriptionService descriptionService) {
        this.roomService = roomService;
        this.descriptionService = descriptionService;
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
}
