package io.socket.nio;

import java.util.concurrent.TimeUnit;

/*
 * NIO通过多路复用Selector不断轮询Channel可以处理多个客户端
 */
public class NioTest {

	final static int PORT = 8888;
	final static String ADDR = "localhost";
	
	public static void main(String[] args) {
		try {
			NioServer.start(PORT);
			TimeUnit.SECONDS.sleep(1);
			NioClient.start(ADDR, PORT);
			TimeUnit.SECONDS.sleep(1);
			NioClient.sendMessage("Hello World!");
			NioClient.sendMessage("I want");
			NioClient.sendMessage("exit");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
