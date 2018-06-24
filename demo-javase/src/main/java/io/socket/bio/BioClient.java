package io.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * 同步阻塞式I/O客户端
 */
public class BioClient {

	public static void connect(String host, int port) {
		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			//建立连接
			socket = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			//发送消息
			writer.println("test");
			System.out.println("send message");
			//读取服务端响应
			String result = reader.readLine();
			System.out.println("client get server message: " + result);
		} catch (Exception e) {
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
