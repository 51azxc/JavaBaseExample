package com.example.spring.aop.service;

public class HelloServiceImpl implements HelloService {

	@Override
	public String sayHello(String s1, String s2) {
		return s1+" "+s2;
	}
}
