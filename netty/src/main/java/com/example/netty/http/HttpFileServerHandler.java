package com.example.netty.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final String url;

	public HttpFileServerHandler(String url) {
		this.url = url;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception {
		
		//请求信息解码失败则返回400错误
		if (!request.decoderResult().isSuccess()) {
			sendError(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		//非GET请求返回405
		if (request.method() != HttpMethod.GET) {
			sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
			return;
		}

		String uri = request.uri();
		if (uri == null || uri.trim().equalsIgnoreCase("")) {
			uri = "/";
		}
		final String path = sanitizeUri(uri);
		//URL错误返回403
		if (path == null) {
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}

		File file = new File(path);
		//文件不存在返回404
		if (file.isHidden() || !file.exists()) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}

		if (file.isDirectory()) {
			if (uri.endsWith("/")) {
				senfListing(ctx, file);
			} else {
				sendRedirect(ctx, uri + "/");
			}
			return;
		}

		if (!file.isFile()) {
			sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
		}

		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}

		Long fileLength = randomAccessFile.length();
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		HttpUtil.setContentLength(httpResponse, fileLength);
		setContentTypeHeader(httpResponse, file);

		if (HttpUtil.isKeepAlive(request)) {
			httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}
		//先写入HTTP Header
		ctx.writeAndFlush(httpResponse);
		//开始写入HTTP body
		ChannelFuture sendFileFuture = ctx.write(
				new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
		sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
			public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
				if (total < 0) {
					System.err.println("progress:" + progress);
				} else {
					System.err.println("progress:" + progress + "/" + total);
				}
			}

			public void operationComplete(ChannelProgressiveFuture future) {
				System.err.println("complete");
			}
		});
		//写入尾部标识
		ChannelFuture lastChannelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
		if (!HttpUtil.isKeepAlive(request)) {
			//写入完毕就关闭连接
			lastChannelFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	public String sanitizeUri(String uri) {
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (Exception e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (Exception ew) {
				ew.printStackTrace();
			}
		}

		if (!uri.startsWith(url) || !uri.startsWith("/")) {
			return null;
		}

		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.') || uri.startsWith(".") || uri.endsWith(".")
				|| INSECURE_URI.matcher(uri).matches()) {
			return null;
		}
		return System.getProperty("user.dir") + File.separator + uri;
	}

	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[a-zA-Z0-9\\.]*");

	private void senfListing(ChannelHandlerContext channelHandlerContext, File dir) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
		StringBuilder builder = new StringBuilder();
		String dirPath = dir.getPath();
		builder.append("<!DOCTYPE html> \r\n");
		builder.append("<html><head><title>");
		builder.append(dirPath);
		builder.append("目录:");
		builder.append("</title></head><body>\r\n");
		builder.append("<h3>");
		builder.append(dirPath).append("目录:");
		builder.append("</h3>\r\n");
		builder.append("<ul>");
		builder.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
		for (File f : dir.listFiles()) {
			if (f.isHidden() || !f.canRead()) {
				continue;
			}
			String fname = f.getName();
			if (!ALLOWED_FILE_NAME.matcher(fname).matches()) {
				continue;
			}
			builder.append("<li>链接：<a href=\" ");
			builder.append(fname);
			builder.append("\" >");
			builder.append(fname);
			builder.append("</a></li>\r\n");
		}
		builder.append("</ul></body></html>\r\n");

		ByteBuf byteBuf = Unpooled.copiedBuffer(builder, CharsetUtil.UTF_8);
		response.content().writeBytes(byteBuf);
		byteBuf.release();
		channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendRedirect(ChannelHandlerContext channelHandlerContext, String newUri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
		response.headers().set(HttpHeaderNames.LOCATION, newUri);
		channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void sendError(ChannelHandlerContext channelHandlerContext, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
				Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private void setContentTypeHeader(HttpResponse httpResponse, File file) {
		MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
		httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getPath()));
	}

}
