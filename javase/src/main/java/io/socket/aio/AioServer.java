package io.socket.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class AioServer {

	private static AsyncServerHandler handler;
	public volatile static long clientCount = 0;

	public static void start(int port) {
		if (handler != null)
			return;
		handler = new AsyncServerHandler(port);
		Executors.newFixedThreadPool(1).execute(handler);
	}
}

class AsyncServerHandler implements Runnable {

	public CountDownLatch latch;
	public AsynchronousServerSocketChannel channel;

	public AsyncServerHandler(int port) {
		try {
			// 创建服务端通道
			channel = AsynchronousServerSocketChannel.open();
			channel.bind(new InetSocketAddress(port));
			System.out.println("server started, port: " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// 阻塞，防止服务端执行完成后退出
		latch = new CountDownLatch(1);
		// 用于接收客户端的连接
		channel.accept(this, new ServerAcceptHandler());
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncServerHandler> {

	@Override
	public void completed(AsynchronousSocketChannel arg0, AsyncServerHandler arg1) {
		// 继续接受其他客户端的请求
		AioServer.clientCount++;
		System.out.println("connected client: " + AioServer.clientCount);
		arg1.channel.accept(arg1, this);
		// 创建新的Buffer
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		// 异步读 第三个参数为接收消息回调的业务Handler
		arg0.read(buffer, buffer, new ServerReadHandler(arg0));
	}

	@Override
	public void failed(Throwable arg0, AsyncServerHandler arg1) {
		arg0.printStackTrace();
		arg1.latch.countDown();
	}
}

class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	// 用于读取半包消息和发送应答
	private AsynchronousSocketChannel channel;

	public ServerReadHandler(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		attachment.flip();
		byte[] message = new byte[attachment.remaining()];
		attachment.get(message);
		try {
			String res = new String(message, "UTF-8");
			System.out.println("server recieved: " + res);
			write(new Date().toString() + " " + res);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		try {
			this.channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void write(String message) {
		byte[] bytes = message.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put(bytes);
		buffer.flip();
		channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				// 如果没有发送完，就继续发送直到完成
				if (buffer.hasRemaining()) {
					channel.write(buffer, buffer, this);
				} else {
					// 创建新的Buffer
					ByteBuffer readBuffer = ByteBuffer.allocate(1024);
					// 异步读 第三个参数为接收消息回调的业务Handler
					channel.read(readBuffer, readBuffer, new ServerReadHandler(channel));
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
	}
}
