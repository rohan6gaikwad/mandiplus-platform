package com.rtech.mandiplus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks real-time online presence for Mandi+ users across active devices and tabs.
 * Broadcasts presence transitions to connected clients over STOMP.
 */
@Service
public class UserOnlineStatusService {

    private static final Logger logger = LoggerFactory.getLogger(UserOnlineStatusService.class);

    // Map: userId -> Set of active WebSocket session IDs
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private SimpMessagingTemplate messagingTemplate;

    public void userConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return;

        boolean wasOffline = false;
        synchronized (userSessions) {
            Set<String> sessions = userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
            wasOffline = sessions.isEmpty();
            sessions.add(sessionId);
        }

        if (wasOffline) {
            logger.info("User {} is now ONLINE (session={})", userId, sessionId);
            broadcastStatus(userId, "ONLINE");
        }
    }

    public void userDisconnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) return;

        boolean isNowOffline = false;
        synchronized (userSessions) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    isNowOffline = true;
                }
            }
        }

        if (isNowOffline) {
            logger.info("User {} is now OFFLINE", userId);
            broadcastStatus(userId, "OFFLINE");
        }
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) return false;
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Set<Long> getOnlineUserIds() {
        return Collections.unmodifiableSet(new HashSet<>(userSessions.keySet()));
    }

    private void broadcastStatus(Long userId, String status) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("status", status);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/online-status", payload);
        } catch (Exception e) {
            logger.warn("Failed to broadcast online status for user {}: {}", userId, e.getMessage());
        }
    }
}
