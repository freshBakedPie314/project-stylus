package com.enigma.projectstylus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private static final long serialVersionUID = 1L;

    private String username;
    private UUID uuid;
    private Long score;
    private Boolean hasSubmitted;
}
