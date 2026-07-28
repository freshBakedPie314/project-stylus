package com.enigma.projectstylus.service;

import com.enigma.projectstylus.dto.RoomJoinResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoomService {

    @Autowired
    public SimpMessagingTemplate simpMessagingTemplate;

    public String createRoom()
    {
        String roomId = UUID.randomUUID().toString().substring(0, 5);
        simpMessagingTemplate.convertAndSend("/topic/room.system", "Room created with ID: " + roomId);
        return roomId;
    }

    public void joinRoom (String roomId, String username)
    {
        simpMessagingTemplate.convertAndSend("/topic/"+roomId, username + "Joined!!");
        //Handle storing in redis
    }
}
