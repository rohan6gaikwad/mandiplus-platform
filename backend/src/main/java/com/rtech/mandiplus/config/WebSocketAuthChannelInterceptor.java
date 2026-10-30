package com.rtech.mandiplus.config;

import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.security.UserDetailsImpl;
import com.rtech.mandiplus.security.UserDetailsServiceImpl;
import com.rtech.mandiplus.service.UserOnlineStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * ChannelInterceptor enforcing JWT authentication on STOMP CONNECT commands
 * and securing private subscription destinations.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOnlineStatusService onlineStatusService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            handleSubscribe(accessor);
        } else if (StompCommand.DISCONNECT.equals(command)) {
            handleDisconnect(accessor);
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);

        if (token != null && jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            accessor.setUser(auth);

            Long userId = null;
            if (userDetails instanceof UserDetailsImpl uImpl) {
                userId = uImpl.getId();
            } else {
                Optional<User> uOpt = userRepository.findByEmail(username);
                if (uOpt.isPresent()) {
                    userId = uOpt.get().getId();
                }
            }

            if (userId != null) {
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null) {
                    sessionAttrs.put("userId", userId);
                    sessionAttrs.put("userEmail", username);
                }
                onlineStatusService.userConnected(userId, accessor.getSessionId());
                logger.info("STOMP authenticated CONNECT: userId={}, email={}, session={}", userId, username, accessor.getSessionId());
            }
        } else {
            logger.warn("STOMP connection rejected: invalid or missing JWT token (session={})", accessor.getSessionId());
            throw new AuthenticationCredentialsNotFoundException("Valid JWT token required to establish WebSocket connection.");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Principal principal = accessor.getUser();
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        Long sessionUserId = sessionAttrs != null ? (Long) sessionAttrs.get("userId") : null;

        if (destination == null) return;

        // Verify authorization for user-specific topic subscriptions
        if (destination.startsWith("/topic/messages.")) {
            String targetIdStr = destination.substring("/topic/messages.".length());
            try {
                Long targetId = Long.parseLong(targetIdStr);
                boolean isAdmin = principal instanceof Authentication a &&
                        a.getAuthorities().stream().anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

                if (!isAdmin && (sessionUserId == null || !sessionUserId.equals(targetId))) {
                    logger.warn("Security Alert: Blocked unauthorized SUBSCRIBE to {} by user {}", destination, sessionUserId);
                    throw new AccessDeniedException("Access denied: You cannot subscribe to another user's private messages.");
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
        if (sessionAttrs != null) {
            Long userId = (Long) sessionAttrs.get("userId");
            if (userId != null) {
                onlineStatusService.userDisconnected(userId, accessor.getSessionId());
                logger.info("STOMP DISCONNECT: userId={}, session={}", userId, accessor.getSessionId());
            }
        }
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // 1. STOMP Native Authorization header: "Bearer eyJhbGci..."
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader)) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }

        // 2. STOMP Native "token" or "passcode" header
        String tokenHeader = accessor.getFirstNativeHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader;
        }

        // 3. Handshake attributes populated by WebSocketHandshakeInterceptor
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("token")) {
            return (String) sessionAttributes.get("token");
        }

        return null;
    }
}
