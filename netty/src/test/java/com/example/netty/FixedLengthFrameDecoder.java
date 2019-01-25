package com.example.netty;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

//继承 ByteToMessageDecoder用来处理入站的字节并将它们解码为消息
public class FixedLengthFrameDecoder extends ByteToMessageDecoder {

    private final int frameLength;

    //指定产出的帧的长度
    public FixedLengthFrameDecoder(int frameLength) {
        if (frameLength <= 0) {
            throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength);
        }
        this.frameLength = frameLength;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
    	//检查是否有足够的字节用于读到下个帧
        if (byteBuf.readableBytes() >= frameLength) {
        	//从ByteBuf读取新帧
            ByteBuf buf = byteBuf.readBytes(frameLength);
            //添加帧到解码好的消息 List
            list.add(buf);
        }
    }
}
