package com.enigma.projectstylus.dto;

import com.enigma.projectstylus.model.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GuessingPhaseInitPayload {
    private GameRoom room;
    private List<DescriptionDTO> descriptions;
}