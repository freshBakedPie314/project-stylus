package com.enigma.projectstylus.dto.daily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyPuzzleDTO {
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<GenreDTO> genres;
    private CreditsDTO credits;

    @Data
    private static class GenreDTO{
        private Integer id;
        private String name;
    }

    public String getFullPosterUrl() {
        return posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : null;
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
