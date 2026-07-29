package com.enigma.projectstylus.model;

import com.enigma.projectstylus.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
    private static final long serialVersionUID = 1L;

    private String roomId;
    private Long time;
    private RoomStatus status;
    private List<Player> players;
}
