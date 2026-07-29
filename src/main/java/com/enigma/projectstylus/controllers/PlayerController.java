package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.model.Player;
import com.enigma.projectstylus.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Player> registerPlayer(@RequestParam String username) {
        return playerService.registerPlayer(username);
    }
}
