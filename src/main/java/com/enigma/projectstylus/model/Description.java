package com.enigma.projectstylus.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Description {
    private UUID id;
    public Long movieId;
    public String movieName;
    public UUID playerId;
    public String playerUsername;
    public String description;
    public String movieUrl;
}
