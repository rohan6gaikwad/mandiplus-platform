package com.rtech.mandiplus.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveFeedWebSocketHandler extends TextWebSocketHandler {

    // All sessions (used for global broadcasts)
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Map userId -> set of sessionIds (a user can have multiple tabs open)
    private static final Map<Long, java.util.Set<String>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);

        // Extract userId from query parameter: ws://host/ws-mandiplus?userId=5
        Long userId = extractUserId(session);
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
            session.getAttributes().put("userId", userId);
            System.out.println("WebSocket connected: session=" + session.getId() + " userId=" + userId);
        } else {
            System.out.println("WebSocket connected: session=" + session.getId() + " (anonymous)");
        }

        // Send a welcome message
        session.sendMessage(new TextMessage("{\"type\":\"SYSTEM\",\"message\":\"Connected to Mandi+ Live Feed.\"}"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());

        // Remove from user sessions map
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            java.util.Set<String> ids = userSessions.get(userId);
            if (ids != null) {
                ids.remove(session.getId());
                if (ids.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        System.out.println("WebSocket disconnected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received WebSocket message: " + message.getPayload());
    }

    /**
     * Broadcast a message to ALL connected sessions (for global events like new product listings).
     */
    public void broadcast(String message) {
        for (WebSocketSession session : sessions.values()) {
            sendIfOpen(session, message);
        }
    }

    /**
     * Send a message only to sessions belonging to a specific user.
     * Falls back to broadcast if userId is null (for backward compatibility).
     */
    public void sendToUser(Long userId, String message) {
        if (userId == null) {
            broadcast(message);
            return;
        }

        java.util.Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            // User not currently online — message is dropped (could be stored in DB for persistence)
            System.out.println("No active WebSocket sessions for userId=" + userId + ". Notification dropped.");
            return;
        }

        for (String sessionId : sessionIds) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null) {
                sendIfOpen(session, message);
            }
        }
    }

    private void sendIfOpen(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("Error sending WebSocket message to session " + session.getId() + ": " + e.getMessage());
            }
        }
    }

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        String query = uri.getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "userId".equals(kv[0])) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
