package io.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NioClient {
	private static NioClientHandler handler;
	
	public static void start(String host, int port) {
		if (handler != null) {
			handler.stop();
		}
		handler = new NioClientHandler(host, port);
		Executors.newFixedThreadPool(1).execute(handler);
	}
	
	public static void sendMessage(String message) {
		if ("exit".equals(message)) {
			return ;
		}
		try {
			handler.sendMessage(message);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class NioClientHandler implements Runnable {
	
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel sc;
	private volatile boolean started;
	
	public NioClientHandler(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			selector = Selector.open();
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			started = true;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		started = false;
	}
	
	private void connect() throws IOException {
		if (sc.connect(new InetSocketAddress(host,port))) {
			System.out.println("client connected successful");
		} else {
			sc.register(selector, SelectionKey.OP_CONNECT);
		}
	}
	
	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (channel.finishConnect()) {
					System.out.println("client connected...");
				} else {
					System.exit(1);
				}
			}
			if (key.isReadable()) {
				//创建ByteBuffer，并开辟一个1M的缓冲区
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				//读取请求码流，返回读取到的字节数
				int readBytes = channel.read(buffer);
				//读取到字节，对字节进行编解码
				if (readBytes > 0) {
					//将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
					buffer.flip();
					//根据缓冲区可读字节数创建字节数组
					byte[] bytes = new byte[buffer.remaining()];
					//将缓冲区可读字节数组复制到新建的数组中
					buffer.get(bytes);
					String result = new String(bytes, "UTF-8");
					System.out.println("client receive: " + result);
				} else if (readBytes < 0) {
					key.cancel();
					channel.close();
				}
			}
		}
	}
	
	public void sendMessage(String msg) throws IOException, InterruptedException {
		if (sc.finishConnect()) {
			TimeUnit.SECONDS.sleep(1);
			sc.register(selector, SelectionKey.OP_READ);
			//将消息编码为字节数组
			byte[] bytes = msg.getBytes();
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			//将字节数组复制到缓冲区
			buffer.put(bytes);
			buffer.flip();
			sc.write(buffer);
		}
	}
	
	@Override
	public void run() {
		try {
			connect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
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
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}