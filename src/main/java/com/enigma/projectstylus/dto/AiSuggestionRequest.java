package com.enigma.projectstylus.dto;

public record AiSuggestionRequest(
        String roomId,
        String userId,
        String movieName,
        String tmdbOverview
) {}