package com.example.spring.boot.security.jwt.dto;

public class WebSocketMessage {
    private String content;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
