package com.example.netty;

import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class FixedLengthFrameDecoderTest {

    @Test
    public void testFramesDecode1() {
        //新建ByteBuf并用字节填充它
        ByteBuf buf = Unpooled.buffer();
        //ByteBuf包含9个可读字节
        for (int i = 0; i < 9; i++) {
            buf.writeByte(i);
        }
        ByteBuf input = buf.duplicate();
        //新增EmbeddedChannel并添加FixedLengthFrameDecoder用于测试
        //FixedLengthFrameDecoder将上述buf被解码成包含了3个可读字节的 ByteBuf
        EmbeddedChannel channel = new EmbeddedChannel(new FixedLengthFrameDecoder(3));

        //写数据到EmbeddedChannel
        Assert.assertFalse(channel.writeInbound(input.readBytes(2)));
        Assert.assertTrue(channel.writeInbound(input.readBytes(7)));

        //标记channel已经完成
        Assert.assertTrue(channel.finish());
        ByteBuf read = (ByteBuf) channel.readInbound();
        Assert.assertEquals(buf.readSlice(3), read);
        read.release();
        
        //调用readInbound()方法来获取EmbeddedChannel中的数据
        read = (ByteBuf) channel.readInbound();
        Assert.assertEquals(buf.readSlice(3), read);
        read.release();
        
        read = (ByteBuf) channel.readInbound();
        Assert.assertEquals(buf.readSlice(3), read);
        read.release();
        
        //全部数据读取完毕
        Assert.assertNull(channel.readInbound());
        buf.release();
    }
}
