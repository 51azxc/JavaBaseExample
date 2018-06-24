package io.socket.aio;

import java.util.concurrent.TimeUnit;

/*
 * 真正的异步非阻塞I/O
 */
public class AioTest {

	final static int PORT = 8888;
	final static String ADDR = "localhost";
	
	public static void main(String[] args) {
		try {
			AioServer.start(PORT);
			TimeUnit.SECONDS.sleep(1);
			AioClient.start(ADDR, PORT);
			TimeUnit.SECONDS.sleep(1);
			AioClient.sendMessage("Hello World!");
			TimeUnit.SECONDS.sleep(1);
			AioClient.sendMessage("I want");
			TimeUnit.SECONDS.sleep(1);
			AioClient.sendMessage("exit");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
