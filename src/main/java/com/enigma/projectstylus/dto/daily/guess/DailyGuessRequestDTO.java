package com.enigma.projectstylus.dto.daily.guess;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyGuessRequestDTO {
    private Long movieId;
    private Integer currentClueOrder;
}
