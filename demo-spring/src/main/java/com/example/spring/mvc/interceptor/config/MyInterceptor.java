package com.example.spring.mvc.interceptor.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class MyInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		System.out.println("preHandle url: "+request.getRequestURI());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		System.out.println("postHandle");
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
		char[] c = new char[1024];
		StringBuffer sb = new StringBuffer();
		int len;
		while((len = br.read(c))>0){
			sb.append(c,0,len);
		}
		br.close();
		System.out.println(sb.toString());
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		System.out.println("afterCompletion");
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
		char[] c = new char[1024];
		StringBuffer sb = new StringBuffer();
		int len;
		while((len = br.read(c))>0){
			sb.append(c,0,len);
		}
		br.close();
		System.out.println(sb.toString());
	}
	
}
