package com.example.netty.websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/*
 * 处理HTTP请求,实现忽略符合"/ws"格式的请求
 */

//扩展 SimpleChannelInboundHandler 用于处理 FullHttpRequest信息
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String wsUri;
    private static final File INDEX;
	private RandomAccessFile file;

    static {
        URL location = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = location.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to locate index.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        if (wsUri.equalsIgnoreCase(fullHttpRequest.uri())) {
            //如果请求是一次升级了的 WebSocket 请求，则递增引用计数器（retain）并且将它传递给
            //在 ChannelPipeline 中的下个 ChannelInboundHandler
        	//retain() 的调用是必要的，因为 channelRead() 完成后，它会调用 FullHttpRequest 上的 release() 来释放其资源
            channelHandlerContext.fireChannelRead(fullHttpRequest.retain());
        } else {
        	//如果客户端发送的 HTTP 1.1 头是“Expect: 100-continue” ，则发送100 Continue的响应
            if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
                //处理符合 HTTP 1.1的 "100 Continue" 请求
                send100Continue(channelHandlerContext);
            }
            file = new RandomAccessFile(INDEX, "r");
            HttpResponse response = new DefaultFullHttpResponse(fullHttpRequest.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
            //判断 keepalive 是否在请求头里面
            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            //写 HttpResponse 到客户端
            channelHandlerContext.write(response);
            //写 index.html 到客户端，根据 ChannelPipeline 中是否有 SslHandler 来决定使用
            //DefaultFileRegion 还是 ChunkedNioFile
            if (channelHandlerContext.pipeline().get(SslHandler.class) == null) {
                channelHandlerContext.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                channelHandlerContext.write(new ChunkedNioFile(file.getChannel()));
            }
            //LastHttpContent标记响应的结束
            ChannelFuture future = channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            //写并刷新 LastHttpContent 到客户端，标记响应完成
            if (!keepAlive) {
                //如果请求头中不包含 keepalive，当写完成时，关闭 Channel
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
