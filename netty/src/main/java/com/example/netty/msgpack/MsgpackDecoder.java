package com.example.netty.msgpack;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {
	protected void decode(io.netty.channel.ChannelHandlerContext arg0, ByteBuf arg1, java.util.List<Object> arg2) throws Exception {
		final byte[] array;
		final int length = arg1.readableBytes();
		array = new byte[length];
		//获取需要解码的byte数组
		arg1.getBytes(arg1.readerIndex(), array, 0, length);
		MessagePack msgpack = new MessagePack();
		//通过reader方法完成反序列化Object对象，加入到解码列表arg2中
		arg2.add(msgpack.read(array));
	}
}
