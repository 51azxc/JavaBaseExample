package com.example.netty.msgpack;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgpackEncoder extends MessageToByteEncoder<Object> {
	@Override
	protected void encode(ChannelHandlerContext arg0, Object arg1, ByteBuf arg2) throws Exception {
		MessagePack msgpack = new MessagePack();
		//将Object编码到byte数组
		byte[] raw = msgpack.write(arg1);
		//写入到ByteBuf中
		arg2.writeBytes(raw);
	}
}
