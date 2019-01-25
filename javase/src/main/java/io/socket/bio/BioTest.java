package io.socket.bio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 同步阻塞式I/O,服务端开启一个线程与客户端建立一个1对1的连接
 * 优点: 简单
 * 缺点: 可伸缩性低，性能低，适合开销小的系统
 */
public class BioTest {

	private final static int PORT = 8888;
	private final static String ADDR = "localhost";
	
	public static void main(String[] args) {
		ExecutorService exec = Executors.newFixedThreadPool(2);
		exec.execute(() -> {
			BioServer.start(PORT);
		});
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		exec.execute(() -> {
			BioClient.connect(ADDR, PORT);
		});
		exec.shutdown();
	}

}
