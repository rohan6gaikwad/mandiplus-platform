package com.rtech.mandiplus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Spring Boot WebSocket with STOMP message broker configuration.
 * Configures endpoints, message broker prefixes, SockJS support, and authentication interceptors.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker routing /topic (broadcast/presence) and /queue (user-specific messages)
        registry.enableSimpleBroker("/topic", "/queue");

        // Application prefix for endpoints mapped to @MessageMapping controllers
        registry.setApplicationDestinationPrefixes("/app");

        // User destination prefix for private user queues (e.g. /user/queue/messages)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary STOMP WebSocket endpoint with pure WebSocket and SockJS fallback
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .withSockJS();

        // Secondary alias endpoint for compatibility
        registry.addEndpoint("/ws-mandiplus-stomp")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor);

        registry.addEndpoint("/ws-mandiplus-stomp")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Enforce JWT token verification on CONNECT and destination authorization on SUBSCRIBE
        registration.interceptors(authChannelInterceptor);
    }
}
