package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.ChatMessageDTO;
import com.rtech.mandiplus.dto.SendMessageRequest;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

/**
 * Controller handling real-time chat messages submitted over STOMP WebSocket.
 */
@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void processMessageFromStomp(Principal principal, @Payload SendMessageRequest request) {
        if (principal == null) {
            logger.warn("Unauthenticated STOMP send attempt rejected");
            return;
        }

        String username = principal.getName();
        Optional<User> senderOpt = userRepository.findByEmail(username);
        if (senderOpt.isEmpty()) {
            logger.warn("Sender user not found for STOMP principal email: {}", username);
            return;
        }

        User sender = senderOpt.get();
        try {
            ChatMessageDTO dto = chatService.sendMessage(sender.getId(), request);
            logger.info("Message processed via STOMP: id={}, from={}, to={}", dto.getId(), sender.getId(), request.getReceiverId());
        } catch (Exception e) {
            logger.error("Failed to process STOMP message from {}: {}", sender.getEmail(), e.getMessage());
        }
    }
}
