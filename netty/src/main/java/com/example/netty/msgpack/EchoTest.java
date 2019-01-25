package com.example.netty.msgpack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 使用MessagePack编解码
 * MessagePack特点: 编解码性能高，码流小。本身不支持处理TCP粘包/半包的问题，需要额外解决
 */

public class EchoTest {
	
	final static int PORT = 8880;
	final static String ADDR = "127.0.0.1";
	
	public static void main(String[] args) {
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.execute(() -> {
			try {
				new EchoServer().bind(PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		service.execute(() -> {
			try {
				new EchoClient(ADDR, PORT, 10).run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
