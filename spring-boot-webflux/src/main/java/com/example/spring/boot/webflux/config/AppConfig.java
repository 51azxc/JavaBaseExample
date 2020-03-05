package com.example.spring.boot.webflux.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

//提供一些UserController需要的依赖
@Configuration
public class AppConfig {
	private final static Logger log = LoggerFactory.getLogger(AppConfig.class);
	
	@Value("${service.url}")
	private String serviceUrl;
	@Value("${http.https.enable}")
	private boolean enableHttps;
	@Value("${http.proxy.enable}")
	private boolean enableProxy;
	@Value("${http.proxy.host}")
	private String host;
	@Value("${http.proxy.port}")
	private int port;

	
	@Bean WebClient webClient() throws SSLException {
		HttpClient httpClient = HttpClient.create();

		//如果要访问https链接需要做如下设置
		if (enableHttps) {
			SslContext sslContext = SslContextBuilder.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();
			httpClient.secure(t -> t.sslContext(sslContext));
		}
		//如果需要代理访问需要做如下配置
		if (enableProxy) {
			int connectTimeout = 60 * 60 * 1000;
			long readTimeout = 100 * connectTimeout;
			httpClient = httpClient.tcpConfiguration(tcpClient ->
					tcpClient
							.proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host(host).port(port))
							.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
							.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))));
		}
		ExchangeFilterFunction logRequest = ExchangeFilterFunction.ofRequestProcessor(request -> {
			log.info("request method: {}", request.method().name());
			return Mono.just(request);
		});
		ExchangeFilterFunction logResponse = ExchangeFilterFunction.ofResponseProcessor(response -> {
			log.info("response status: {}", response.statusCode().name());
			return Mono.just(response);
		});

		ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		return WebClient.builder().clientConnector(connector)
				.filter(logRequest).filter(logResponse)
				.baseUrl(serviceUrl).build();
	}
}
