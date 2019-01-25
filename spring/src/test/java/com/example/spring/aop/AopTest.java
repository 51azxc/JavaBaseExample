package com.example.spring.aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.example.spring.aop.service.HelloService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=HelloConfig.class, loader=AnnotationConfigContextLoader.class)
public class AopTest {

	@Autowired HelloService helloService;
	
	@Test
	public void testHello() {
		System.out.println(helloService.sayHello("Hello","World"));
	}
}
