package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.DescriptionDTO;
import com.enigma.projectstylus.service.DescriptionService;
import com.enigma.projectstylus.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    private final RoomService roomService;
    private final DescriptionService descriptionService;

    public RoomRestController(RoomService roomService,  DescriptionService descriptionService) {
        this.roomService = roomService;
        this.descriptionService = descriptionService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRoom() {
        String roomId = roomService.createRoom();
        return ResponseEntity.ok(roomId);
    }

    @GetMapping("/descriptions")
    public ResponseEntity<List<DescriptionDTO>> getAllDescriptions(@RequestParam String roomId) {
        return ResponseEntity.ok(descriptionService.getAllDescriptions(roomId));
    }
}