package com.enigma.projectstylus.dto.daily;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

    private List<CluesDTO> clues;
    private Map<Integer, String> blurredPosterUrls;


    @Data
    public static class GenreDTO{
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
    }

    @Data
    public static class CluesDTO {
        private Integer order;
        private String text;
    }

    public void fromDailyPuzzleTmdbResponseDTO(DailyPuzzleTmdbResponseDTO source) {
        if (source == null)
        {
            return;
        }

        this.id = source.getId();
        this.title = source.getTitle();
        this.overview = source.getOverview();
        this.posterPath = source.getPosterPath();

        List<CastDto> castDtoList = List.of();
        if (source.getCredits() != null && source.getCredits().getCast() != null)
        {
            castDtoList = source.getCredits().getCast().stream()
                    .limit(3)
                    .map(cast -> {
                        CastDto curr = new CastDto();
                        curr.setName(cast.getName());
                        return curr;
                    })
                    .toList();
        }

        CreditsDTO creditsDTO = new CreditsDTO();
        creditsDTO.setCast(castDtoList);
        this.credits = creditsDTO;

        List<GenreDTO>  genreDtoList = List.of();

        if(source.getGenres() != null)
        {
            genreDtoList = source.getGenres().stream().limit(3)
                    .map(genre -> {
                        GenreDTO genreDTO = new GenreDTO();
                        genreDTO.setName(genre.getName());
                        return genreDTO;
                    }).toList();
        }
        this.genres = genreDtoList;
    }
}
