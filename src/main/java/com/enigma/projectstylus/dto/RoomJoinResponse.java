package com.enigma.projectstylus.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomJoinResponse {
    @JsonProperty("room_id")
    private  String roomId;
    @JsonProperty("users")
    private List<String> users;
}
