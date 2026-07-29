package com.enigma.projectstylus.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Message {
    private String user;
    private String message;
    private LocalDate timestamp;
}
