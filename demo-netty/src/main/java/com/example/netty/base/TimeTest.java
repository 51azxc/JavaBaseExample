package com.example.netty.base;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeTest {
	
	final static int PORT = 8888;
	final static String ADDR = "127.0.0.1";

	public static void main(String[] args){
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.execute(() -> {
			try {
				new TimeServer().bind(PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		service.execute(() -> {
			try {
				new TimeClient().connect(ADDR, PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
