package com.enigma.projectstylus.dto.daily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyPuzzleTmdbResponseDTO {
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<GenreDTO> genres;
    private CreditsDTO credits;

    @Data
    public static class GenreDTO{
        private Integer id;
        private String name;
    }

    @Data
    public static class CreditsDTO {
        private List<CastDto> cast;
    }

    @Data
    public static class CastDto {
        private String name;
        private String character;
        private Integer order;
    }
}
