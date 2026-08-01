package com.enigma.projectstylus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreatioonPayload {
    private int writingTime;
    private int guessingTime;
}
