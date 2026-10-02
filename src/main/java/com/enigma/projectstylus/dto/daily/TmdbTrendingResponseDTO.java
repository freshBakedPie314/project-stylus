package com.enigma.projectstylus.dto.daily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbTrendingResponseDTO {
    private List<TrendingMovieItem> results;

    @Data
    public static class TrendingMovieItem {
        private Long id;
        private String title;
        private String overview;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("vote_count")
        private Integer voteCount;
    }
}