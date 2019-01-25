package com.example.netty.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {
	
	private class TimeServerHandler extends ChannelInboundHandlerAdapter{
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			ByteBuf buf = (ByteBuf) msg;
			byte[] req = new byte[buf.readableBytes()];
			buf.readBytes(req);
			//获取到客户端请求的数据
			String body = new String(req, "UTF-8");
			System.out.println("The time server receive order: " + body);
			String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? 
					new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
			ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
			//异步发送消息给客户端
			ctx.write(resp);
		}
		
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			//将缓冲区消息全部写到SocketChannel中
			ctx.flush();
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			ctx.close();
		}
	}
	
	public void bind(int port) throws Exception {
		//NioEventLoopGroup为线程组，专门用于处理网络事件，即Reactor数组
		//用于接受客户端的连接
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		//用于进行SocketChannel读写
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			//辅助启动类，降低复杂度
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new TimeServerHandler());
					}
				});
			//同步等待
			ChannelFuture f = b.bind(port).sync();
			//等待服务端监听端口关闭
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	
}
