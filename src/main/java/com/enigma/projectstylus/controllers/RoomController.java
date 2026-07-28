package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Controller
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;

    }

    @MessageMapping("/room.join/{roomId}")
    public void joinRoom(@DestinationVariable String roomId, @Payload String username) {
        roomService.joinRoom(roomId, username);
    }
}
