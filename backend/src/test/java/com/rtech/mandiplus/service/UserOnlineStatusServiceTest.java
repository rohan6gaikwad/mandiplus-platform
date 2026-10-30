package com.rtech.mandiplus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserOnlineStatusServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private UserOnlineStatusService statusService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("User connects first session - marked online and broadcast sent")
    void testUserConnectsFirstSession() {
        Long userId = 100L;
        String session1 = "sess-1";

        assertFalse(statusService.isUserOnline(userId));

        statusService.userConnected(userId, session1);

        assertTrue(statusService.isUserOnline(userId));
        assertTrue(statusService.getOnlineUserIds().contains(userId));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/online-status"), any(Map.class));
    }

    @Test
    @DisplayName("User connects multiple tabs - only single ONLINE broadcast triggered")
    void testUserMultipleTabs() {
        Long userId = 101L;

        statusService.userConnected(userId, "sess-A");
        statusService.userConnected(userId, "sess-B");

        assertTrue(statusService.isUserOnline(userId));
        // Broadcast should only be sent once when transitioning from offline to online
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/online-status"), any(Map.class));

        // Close one tab
        statusService.userDisconnected(userId, "sess-A");
        // Still online because sess-B is open
        assertTrue(statusService.isUserOnline(userId));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/online-status"), any(Map.class));

        // Close second tab
        statusService.userDisconnected(userId, "sess-B");
        assertFalse(statusService.isUserOnline(userId));
        // OFFLINE broadcast triggered
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/online-status"), any(Map.class));
    }
}
