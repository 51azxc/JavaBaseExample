package com.example.netty.data.adhering;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 使用DelimiterBasedFrameDecoder解决TCP粘包/拆包问题
 * DelimiterBasedFrameDecoder是自定义分隔符
 * LineBasedFrameDecoder是通过换行符"\n"或者"\r\n"来分割传输数据
 * FixedLengthFrameDecoder是通过固定长度来分割
 */

public class EchoTest {

	final static int PORT = 8888;
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
				new EchoClient().connect(ADDR, PORT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
