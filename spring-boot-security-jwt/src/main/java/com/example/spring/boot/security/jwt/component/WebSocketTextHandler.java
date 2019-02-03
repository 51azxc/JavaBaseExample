package com.example.spring.boot.security.jwt.component;

import com.example.spring.boot.security.jwt.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class WebSocketTextHandler extends TextWebSocketHandler {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketTextHandler.class);

    @Autowired
    ObjectMapper mapper;

    //存储当前连接的用户
    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //如果用户已登陆则存入用户会话
        Optional.ofNullable(session.getPrincipal().getName()).ifPresent(username ->sessions.add(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error:" + exception.getLocalizedMessage());
        if (session.isOpen()) { session.close(); }
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //session.sendMessage(new TextMessage("Hello " + message.getPayload()));
        String msg = "Hello " + message.getPayload();
        sendMessageToUser(session.getPrincipal().getName(), new WebSocketMessage(msg));
    }

    //发送消息给指定用户
    public void sendMessageToUser(String username, WebSocketMessage message) {
        Optional<WebSocketSession> session = sessions.stream().
                filter(s -> s.getPrincipal().getName().equals(username)).findFirst();
        if (session.isPresent()) {
            try {
                session.get().sendMessage(new TextMessage(mapper.writeValueAsString(message)));
            } catch (IOException e) {
                logger.warn("send message to user error: " + e.getLocalizedMessage());
            }
        }
    }

}
