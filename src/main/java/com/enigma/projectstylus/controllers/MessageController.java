package com.enigma.projectstylus.controllers;

import com.enigma.projectstylus.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/send/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, @Payload Message message) {
        System.out.println("Sending Message : " + message);
        simpMessagingTemplate.convertAndSend("/topic/" + roomId, message);
    }
}
