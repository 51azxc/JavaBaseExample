package com.example.spring.boot.security.jwt.config;

import com.example.spring.boot.security.jwt.component.WebSocketHandshakeHandler;
import com.example.spring.boot.security.jwt.component.WebSocketHandshakeInterceptor;
import com.example.spring.boot.security.jwt.component.WebSocketTextHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// WebSocket原生协议配置文件

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
    @Autowired
    WebSocketHandshakeHandler webSocketHandshakeHandler;
    @Autowired
    WebSocketTextHandler webSocketTextHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketTextHandler, "/ws")
                .addInterceptors(webSocketHandshakeInterceptor)
                .setHandshakeHandler(webSocketHandshakeHandler)
                .setAllowedOrigins("*");
    }
}
