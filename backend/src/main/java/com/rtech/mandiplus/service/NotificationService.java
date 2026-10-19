package com.rtech.mandiplus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtech.mandiplus.config.LiveFeedWebSocketHandler;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private LiveFeedWebSocketHandler webSocketHandler;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Safely extracts the ID from a User entity, forcing initialization if it's a lazy proxy
     * to avoid null identifier values in getter calls on uninitialized Hibernate proxies.
     */
    public static Long getUserIdSafely(User user) {
        if (user == null) return null;
        try {
            user.getEmail(); // Forces initialization of lazy Hibernate proxy
        } catch (Exception e) {
            System.err.println("Failed to force initialize user proxy: " + e.getMessage());
        }
        return user.getId();
    }

    /**
     * Send a notification to a specific user (or broadcast to all if userId is null).
     * When userId is non-null, only that user's WebSocket sessions receive the message.
     * When userId is null, all connected sessions receive the message (global broadcast).
     */
    public void sendNotification(Long userId, String title, String message, String category) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "ALERT");
            alert.put("userId", userId);
            alert.put("title", title);
            alert.put("message", message);
            alert.put("category", category);
            alert.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(alert);

            if (userId == null) {
                // Double check category: only global updates should ever be broadcast to all
                if ("MARKETPLACE".equalsIgnoreCase(category) || "SYSTEM".equalsIgnoreCase(category) || "MANDI_PRICE".equalsIgnoreCase(category)) {
                    webSocketHandler.broadcast(json);
                } else {
                    System.err.println("Blocked attempt to broadcast private category (" + category + ") globally! Title: " + title);
                }
            } else {
                // Targeted delivery — only sends to the specified user's sessions
                webSocketHandler.sendToUser(userId, json);
            }
        } catch (Exception e) {
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
    }

    public boolean broadcastNotification(String title, String message, String category) {
        if ("PAYMENT".equalsIgnoreCase(category) || "ORDER".equalsIgnoreCase(category) || "PRIVATE".equalsIgnoreCase(category)) {
            System.err.println("Blocked attempt to broadcast private category (" + category + ") globally!");
            return false;
        }
        sendNotification(null, title, message, category);
        return true;
    }

    /**
     * Send targeted notification to all admins in the system.
     */
    public void sendToAdmin(String title, String message, String category) {
        try {
            List<User> admins = userRepository.findByRole("ADMIN");
            if (admins != null) {
                for (User admin : admins) {
                    sendNotification(admin.getId(), title, message, category);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to notify database admins: " + e.getMessage());
        }
        // Also send to mock frontend UI admin (ID 999)
        sendNotification(999L, title, message, category);
    }
}
