package io.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * 同步阻塞式I/O服务端
 */
public class BioServer {
	
	public static void start(int port) {
		ServerSocket ss = null;
		try {
			//监听8888端口创建连接
			ss = new ServerSocket(port);
			System.out.println("server started in port: " + port);
			Socket socket = null;
			//无限循环监听客户端的连接
			while (true) {
				//如果收到了客户端连接，就交给handler处理，没有就继续阻塞
				socket = ss.accept();
				new Thread(new BioServerHandler(socket)).start();
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (ss != null) {
				try {
					ss.close();
					ss = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static class BioServerHandler implements Runnable {

		private Socket socket;
		
		public BioServerHandler(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			BufferedReader reader = null;
			PrintWriter writer = null;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
				String result;
				//读取客户端传入数据，直接输出
				while ((result = reader.readLine()) != null) {
					System.out.println("server received message: " + result);
					writer.println("Hello " + result);
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (writer != null) {
						writer.close();
					}
					if (socket != null) {
						socket.close();
						socket = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
}
