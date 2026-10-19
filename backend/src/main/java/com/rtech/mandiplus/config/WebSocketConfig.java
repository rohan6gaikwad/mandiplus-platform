package com.rtech.mandiplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LiveFeedWebSocketHandler webSocketHandler;

    public WebSocketConfig(LiveFeedWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the handler for path /ws-mandiplus, allowing all cross-origin requests
        registry.addHandler(webSocketHandler, "/ws-mandiplus")
                .setAllowedOrigins("*");
    }
}
