package com.example.spring.boot.security.jwt.config;

import com.example.spring.boot.security.jwt.dto.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Bean WebSocketHandshakeInterceptor webSocketHandshakeInterceptor() {
        return new WebSocketHandshakeInterceptor();
    }
    @Bean WebSocketChannelInterceptor webSocketChannelInterceptor() {
        return new WebSocketChannelInterceptor();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //topic通常为广播路径，queue通常为点对点路径
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");             //设置用户接收信息前缀
        registry.setApplicationDestinationPrefixes("/app");     //设置发送消息前缀
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                         //指定websocket连接路径
                .setAllowedOrigins("*")                             //允许跨域
                //.addInterceptors(webSocketHandshakeInterceptor())   //在通过传入的token参数判断用户是否已登陆
                //.setHandshakeHandler(defaultHandshakeHandler())     //获取解析当前用户，并放入会话中
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        //也可通过ChannelInterceptor来判断用户是否登陆，这里是通过自定义头属性来获取token
        //如果需要用这个，就需要通过@Order注解来设置优先级高于Spring Security的配置
        registration.interceptors(webSocketChannelInterceptor());
    }

    private DefaultHandshakeHandler defaultHandshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                String username = (String)attributes.get("username");
                if (username != null && !"".equals(username)) {
                    return  new UserPrincipal(username);
                }
                return super.determineUser(request, wsHandler, attributes);
            }
        };
    }
}
