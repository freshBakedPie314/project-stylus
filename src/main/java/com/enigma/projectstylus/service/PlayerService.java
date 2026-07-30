package com.enigma.projectstylus.service;

import com.enigma.projectstylus.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlayerService {
    public ResponseEntity<Player> registerPlayer(String username)
    {
        return ResponseEntity.ok(
                Player.builder()
                        .username(username)
                        .id(UUID.randomUUID())
                        .score(0L)
                        .hasSubmitted(false)
                        .totalGuessed(0L)
                .build()
        );
    }
}
