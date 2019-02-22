package com.example.spring.boot.webflux.security.jwt.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

//h2 web console基于servlet，因此需要另外开一个服务才能访问
@Component
public class H2Config {

    private org.h2.tools.Server webServer;
    private org.h2.tools.Server server;

    @EventListener(org.springframework.context.event.ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
        this.webServer = org.h2.tools.Server.createWebServer("-webPort", "8089", "-tcpAllowOthers").start();
        this.server = org.h2.tools.Server.createTcpServer("-tcpPort", "8090", "-tcpAllowOthers").start();
    }

    @EventListener(org.springframework.context.event.ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
        this.server.stop();
    }

}
