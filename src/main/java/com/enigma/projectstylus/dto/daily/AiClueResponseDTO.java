package com.enigma.projectstylus.dto.daily;

import lombok.Data;

import java.util.List;

@Data
public class AiClueResponseDTO {
    private List<DailyPuzzleDTO.CluesDTO> clues;
}
