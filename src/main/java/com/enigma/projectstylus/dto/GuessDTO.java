package com.enigma.projectstylus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuessDTO {
    private UUID playerId;
    private UUID descriptionId;
    private Long guessedMovieId;
}

