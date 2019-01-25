package com.example.netty.marshall;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * JBOSS Marshalling是一个Java对象序列化包，对JDK默认的序列化框架进行了优化，
 * 也解决了TCP粘包/半包的问题
 */

public class UserTest {
	final static int PORT = 8888;
	final static String ADDR = "127.0.0.1";
	
	public static void main(String[] args) {
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.execute(() -> {
			try {
				new UserServer().bind(PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		service.execute(() -> {
			try {
				new UserClient().connect(ADDR, PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
