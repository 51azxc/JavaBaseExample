package com.example.spring.boot.security.jwt.config;

import com.example.spring.boot.security.jwt.dto.UserPrincipal;
import com.example.spring.boot.security.jwt.exception.UnAuthenticationException;
import com.example.spring.boot.security.jwt.service.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.validation.ValidationUtils;

import java.util.Optional;

public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Autowired JwtTokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            //连接时获取头部的token属性值
            Optional<String> username = Optional.ofNullable(accessor.getFirstNativeHeader("token"))
                    .map(tokenProvider::decode);
            if (username.isPresent()) {
                accessor.setUser(new UserPrincipal(username.get()));
            } else {
                return null;
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            UserPrincipal userPrincipal = (UserPrincipal) accessor.getHeader("simpUser");
            if (userPrincipal == null || "".equals(userPrincipal.getName())) {
                return null;
            }
        }
        return message;
    }

}
