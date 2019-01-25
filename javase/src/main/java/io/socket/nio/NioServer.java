package io.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;

public class NioServer {
	private static NioServerHandler handler;

	public static void start(int port) {
		if (handler != null) {
			handler.stop();
		}
		handler = new NioServerHandler(port);
		Executors.newSingleThreadExecutor().execute(handler);
	}
}

class NioServerHandler implements Runnable {
	
	private Selector selector;
	private ServerSocketChannel channel;
	private volatile boolean started;
	
	public NioServerHandler(int port) {
		try {
			selector = Selector.open();
			channel = ServerSocketChannel.open();
			//非阻塞
			channel.configureBlocking(false);
			channel.socket().bind(new InetSocketAddress(port), 1024);
			channel.register(selector, SelectionKey.OP_ACCEPT);
			started = true;
			System.out.println("server started! port: " + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		started = false;
	}
	
	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			//处理新接入的请求消息
			if (key.isAcceptable()) {
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				//通过ServerSocketChannel的accept创建SocketChannel实例  
                //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				//注册为读
				sc.register(selector, SelectionKey.OP_READ);
			}
			//读消息
			if (key.isReadable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				//创建ByteBuffer，并开辟一个1M的缓冲区
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				//读取请求码流，返回读取到的字节数
				int readBytes = sc.read(buffer);
				//读取到字节，对字节进行编解码
				if (readBytes > 0) {
					//将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
					buffer.flip();
					//根据缓冲区可读字节数创建字节数组
					byte[] bytes = new byte[buffer.remaining()];
					//将缓冲区可读字节数组复制到新建的数组中
					buffer.get(bytes);
					String expression = new String(bytes, "UTF-8");
					System.out.println("server receive: " + expression);
					String result = new Date().toString() + " " + expression;
					byte[] writeBytes = result.getBytes();
					ByteBuffer writeBuffer = ByteBuffer.allocate(writeBytes.length);
					//将字节数组复制到缓冲区
					writeBuffer.put(writeBytes);
					writeBuffer.flip();
					//发送缓冲区的字节数组
					sc.write(writeBuffer);
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				}
			}
		}
	}
	
	@Override
	public void run() {
		while(started) {
			try {
				//无论是否有读写事件发生，selector每隔1s被唤醒一次
				selector.select(1000);
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				SelectionKey key = null;
				while(it.hasNext()) {
					key = it.next();
					it.remove();
					handleInput(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//selector关闭后会自动释放里面管理的资源
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
