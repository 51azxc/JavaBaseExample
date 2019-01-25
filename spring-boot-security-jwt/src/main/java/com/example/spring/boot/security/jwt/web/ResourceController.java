package com.example.spring.boot.security.jwt.web;

import com.example.spring.boot.security.jwt.dto.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.concurrent.TimeUnit;

@Controller
public class ResourceController {

    @Autowired SimpMessagingTemplate template;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/")
    public HttpEntity<?> index() {
        return ResponseEntity.ok("Hello World");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/resource/user")
    public HttpEntity<?> getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails)auth.getPrincipal();
        return ResponseEntity.ok("Hello User " + userDetails.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/resource/admin")
    public HttpEntity<?> getAdminUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails)auth.getPrincipal();
        return ResponseEntity.ok("Hello Admin " + userDetails.getUsername());
    }

    @MessageMapping("/hello")       //接收客户端消息
    @SendToUser("/queue/hello")     //发送信息给订阅消息的用户
    public WebSocketMessage webSocketMessage(WebSocketMessage message, Principal user) throws Exception {
        String msg = message.getContent();
        for (int i = 1; i <= 5; i++) {
            TimeUnit.SECONDS.sleep(1);
            //发送给指定用户
            template.convertAndSendToUser(user.getName(), "/queue/hello/msg",
                    new WebSocketMessage(msg + " " + i));
        }

        return new WebSocketMessage("hello " + msg);
    }

}
