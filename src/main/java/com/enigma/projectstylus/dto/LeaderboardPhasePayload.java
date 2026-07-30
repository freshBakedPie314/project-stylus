package com.enigma.projectstylus.dto;

import com.enigma.projectstylus.model.Description;
import com.enigma.projectstylus.model.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaderboardPhasePayload {
    private GameRoom room;
    private List<Description> descriptions;
}
