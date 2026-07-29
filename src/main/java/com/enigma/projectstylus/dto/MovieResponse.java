package com.enigma.projectstylus.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieResponse {
    private String name;
    private String description;
    private String movieUrl;
    private Long id;
}
