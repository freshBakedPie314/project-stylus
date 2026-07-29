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
public class DescriptionDTO {
    private UUID id;
    private String description;
    private UUID playerId;
    private String playerUsername;
}
