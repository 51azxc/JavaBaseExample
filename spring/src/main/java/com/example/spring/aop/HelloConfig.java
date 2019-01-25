package com.example.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.example.spring.aop.aspect.HelloAspect;
import com.example.spring.aop.service.HelloService;
import com.example.spring.aop.service.HelloServiceImpl;

@Configuration
@EnableAspectJAutoProxy
//如果配置了以下自动扫描就无需写下边的@Bean配置了，不过需要在对应的类上加上@Componet,@Service注解
//@ComponentScan({"com.example.spring.aop.service", "com.example.spring.aop.aspect"})
public class HelloConfig {
	
	@Bean HelloService helloService() {
		return new HelloServiceImpl();
	}
	
	@Bean HelloAspect helloAspect() {
		return new HelloAspect();
	}

}
