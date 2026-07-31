package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.AiSuggestionRequest;
import com.enigma.projectstylus.service.AISuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(originPatterns = "*")
public class AiController {

    private final AISuggestionService aiSuggestionService;

    public AiController(AISuggestionService aiSuggestionService) {
        this.aiSuggestionService = aiSuggestionService;
    }

    @PostMapping("/trigger-suggestions")
    public ResponseEntity<Void> triggerSuggestions(@RequestBody AiSuggestionRequest request) {

        aiSuggestionService.generateAndBroadcastSuggestions(
                request.roomId(),
                request.userId(),
                request.movieName(),
                request.tmdbOverview()
        );

        // Empty 202 for cinfirmation
        return ResponseEntity.accepted().build();
    }
}