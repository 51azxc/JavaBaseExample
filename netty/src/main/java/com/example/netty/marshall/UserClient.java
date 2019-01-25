package com.example.netty.marshall;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class UserClient {
	public void connect(String host, int port) {
		EventLoopGroup group = new NioEventLoopGroup();
		try { 
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingDecoder());
						ch.pipeline().addLast(MarshallingCodeCFactory.buildMarshallingEncoder());
						ch.pipeline().addLast(new UserClientHandler());
					}
				});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
	public static void main(String[] args) {
		new UserClient().connect("localhost", 8888);
	}
}

class UserClientHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		for (int i = 0; i < 10; i++) {
			UserInfo userInfo = new UserInfo();
			userInfo.setName("a"+i);
			userInfo.setAge(10+i);
			ctx.write(userInfo);
		}
		ctx.flush();
	}
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Client receive response: " + msg.toString());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		cause.printStackTrace();
	}
}
