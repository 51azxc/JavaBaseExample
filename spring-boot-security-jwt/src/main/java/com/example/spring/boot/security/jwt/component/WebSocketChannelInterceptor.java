package com.example.spring.boot.security.jwt.component;

import com.example.spring.boot.security.jwt.dto.UserPrincipal;
import com.example.spring.boot.security.jwt.exception.UnAuthenticationException;
import com.example.spring.boot.security.jwt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.Optional;

public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);

    @Autowired JwtTokenProvider tokenProvider;
    @Autowired UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            //连接时获取头部的token属性值
            Optional<String> token = Optional.ofNullable(accessor.getFirstNativeHeader("token"));
            if (token.isPresent()) {
                try {
                    Long userId = tokenProvider.decode(token.get());
                    Optional<UserPrincipal> userPrincipal = userService.getUserPrincipalById(userId);
                    if (userPrincipal.isPresent()) {
                        accessor.setUser(userPrincipal.get());
                        return message;
                    }
                } catch (UnAuthenticationException e) {
                    logger.warn(e.getLocalizedMessage());
                }
            }
            return null;
        } else if (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            UserPrincipal userPrincipal = (UserPrincipal) accessor.getHeader("simpUser");
            if (userPrincipal == null || "".equals(userPrincipal.getName())) {
                logger.warn("User " + accessor.getSessionId() + " is not login in.");
                return null;
            }
        }
        return message;
    }

}
