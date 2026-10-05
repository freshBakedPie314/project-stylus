package com.enigma.projectstylus.model;

import com.enigma.projectstylus.RoomStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameRoom {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private Long time;
    private RoomStatus status;
    private List<Player> players;
    private Long totalDone;

    private int writingLimit = 60;
    private int guessingLimit = 60;

    private Long phaseEndTime;

    // descriptions are only sent when joining in GUESSING phase
    private List<Description> descriptions;
}
