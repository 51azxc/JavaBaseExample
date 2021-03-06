package com.example.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/*
 * 构建一个HTTP访问文件服务
 */

public class HttpFileServer {
	private final static String PATH = "/src/main/java/";
	private final static int PORT = 8888;
	
	public void run(final String url, final int port) {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("http-request-encoder and http-response-decoder", new HttpServerCodec());
					//将多个消息转换成一个FullHttpRequest/FullHttpResponse
					ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
					//支持异步发送大码流（大文件传输）
					ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
					ch.pipeline().addLast("fileServerHandler", new HttpFileServerHandler(url));
				}
			});
		try {
			ChannelFuture f = b.bind(port).sync();
			System.out.println("http://localhost:"+port+url);
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		new HttpFileServer().run(PATH, PORT);
	}
}
