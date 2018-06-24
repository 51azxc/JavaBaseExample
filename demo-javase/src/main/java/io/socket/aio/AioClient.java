package io.socket.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class AioClient {
	private static AsyncClientHandler handler;

	public static void start(String host, int port) {
		if (handler != null)
			return;
		handler = new AsyncClientHandler(host, port);
		Executors.newSingleThreadExecutor().execute(handler);
	}

	public static void sendMessage(String message) {
		if ("exit".equals(message)) {
			return;
		}
		handler.sendMessage(message);
	}
}

class AsyncClientHandler implements CompletionHandler<Void, AsyncClientHandler>, Runnable {

	private AsynchronousSocketChannel channel;
	private String host;
	private int port;
	private CountDownLatch latch;
	
	public AsyncClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			channel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//向服务器发送消息
	public void sendMessage(String msg) {
		byte[] bytes = msg.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		channel.write(buffer, buffer, new ClientWriteHandler(channel, latch));
	}
	
	@Override
	public void run() {
		//创建CountDownLatch等待
		latch = new CountDownLatch(1);
		//发起异步连接操作，回调参数就是这个类本身，如果连接成功会回调completed方法
		channel.connect(new InetSocketAddress(host, port), this, this);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void completed(Void arg0, AsyncClientHandler arg1) {
		System.out.println("client connect successful");
	}

	@Override
	public void failed(Throwable arg0, AsyncClientHandler arg1) {
		System.out.println("client connect failed, error: " + arg0.getLocalizedMessage());
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}
}

class ClientReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	private AsynchronousSocketChannel channel;
	private CountDownLatch latch;
	
	public ClientReadHandler(AsynchronousSocketChannel channel, CountDownLatch latch) {
		this.channel = channel;
		this.latch = latch;
	}
	
	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		attachment.flip();
		byte[] bytes = new byte[attachment.remaining()];
		attachment.get(bytes);
		try {
			String message = new String(bytes, "UTF-8");
			System.out.println("client get message: " + message);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		System.out.println("get message error: " + exc.getLocalizedMessage());
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}
}

class ClientWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

	private AsynchronousSocketChannel channel;
	private CountDownLatch latch;
	
	public ClientWriteHandler(AsynchronousSocketChannel channel, CountDownLatch latch) {
		this.channel = channel;
		this.latch = latch;
	}
	
	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		//完成全部数据的写入
		if (attachment.hasRemaining()) {
			channel.write(attachment, attachment, this);
		} else {
			//读取数据
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);
			channel.read(readBuffer, readBuffer, new ClientReadHandler(channel, latch));
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		System.out.println("send message error: " + exc.getLocalizedMessage());
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		latch.countDown();
	}
}


