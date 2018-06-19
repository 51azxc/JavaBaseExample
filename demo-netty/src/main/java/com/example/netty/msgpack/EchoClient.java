package com.example.netty.msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class EchoClient {
	
	private final String host;
	private final int port;
	private final int sendNumber;
	
	public EchoClient(String host, int port, int sendNumber) {
		this.host = host;
		this.port = port;
		this.sendNumber = sendNumber;
	}
	
	public static void main(String[] args) throws Exception {
		new EchoClient("localhost", 8880, 10).run();
	}

	public void run() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel arg0) throws Exception {
						arg0.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024, 0,  2, 0, 2));
						arg0.pipeline().addLast("msgpack decoder", new MsgpackDecoder());
						arg0.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
						arg0.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
						arg0.pipeline().addLast(new EchoClientHandler(sendNumber));
					}
				});
			ChannelFuture f = b.connect(host, port).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}

	class EchoClientHandler extends ChannelHandlerAdapter {
		private final int sendNumber;
		public EchoClientHandler(int sendNumber) {
			this.sendNumber = sendNumber;
		}
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			UserInfo[] infos = userInfo();
			for (UserInfo info:infos) {
				ctx.write(info);
			}
			ctx.flush();
		}
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("Client receive the msgpack message: " + msg.toString());
			ctx.write(msg);
		}
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
		
		private UserInfo[] userInfo() {
			UserInfo[] infos = new UserInfo[sendNumber];
			UserInfo userInfo = null;
			for (int i = 0; i < sendNumber; i++) {
				userInfo = new UserInfo();
				userInfo.setName("A"+i);
				userInfo.setAge(10+i);
				infos[i] = userInfo;
			}
			return infos;
		}
	}
}
