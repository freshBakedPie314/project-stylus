package com.enigma.projectstylus.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuessResponseDTO {
    public UUID descriptionId;
    private Boolean correct;
    private String movieName;
    private String movieUrl;
    private String movieDescription;
}
