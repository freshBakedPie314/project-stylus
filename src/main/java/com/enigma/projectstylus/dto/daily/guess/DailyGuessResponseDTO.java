package com.enigma.projectstylus.dto.daily.guess;

import com.enigma.projectstylus.dto.daily.DailyClueDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyGuessResponseDTO {
    private boolean correct;
    private boolean gameOver;
    private String message;

    private DailyClueDTO nextClue;

    private String movieTitle;
    private String posterPath;
}
