Swagger2可以生成Rest API的接口访问页面。

运行`Application.java`成功后可以访问  http://localhost:8080/swagger-ui.html 查看效果

Swagger2基于SpringMVC及Tomcat，因此不支持Spring boot 2 with Reactive(Reactive需要Netty支持),如果需要使用Reactive特性，可以参考https://github.com/armdev/springboot2-swagger