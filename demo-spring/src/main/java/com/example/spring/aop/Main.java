package com.example.spring.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.spring.aop.service.HelloService;

public class Main {
	
	private static ApplicationContext ctx;

	public static void main(String[] args) {
		ctx = new AnnotationConfigApplicationContext(HelloConfig.class);
		HelloService hello = (HelloService) ctx.getBean("helloService");
		hello.sayHello("Hello", "World");
	}

}
