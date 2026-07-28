package com.enigma.projectstylus.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Message {
    private String user;
    private String message;
    private LocalDate timestamp;
}
