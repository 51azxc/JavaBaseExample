package com.example.spring.mvc.interceptor.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter(filterName="MyFilter",urlPatterns={"/*"})
public class MyFilter implements Filter {
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		//HttpServletResponse res = (HttpServletResponse) response;
		System.out.println("filter");
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"UTF-8"));
		char[] c = new char[1024];
		StringBuffer sb = new StringBuffer();
		int len;
		while((len = br.read(c))>0){
			sb.append(c,0,len);
		}
		br.close();
		System.out.println(sb.toString());
		
		chain.doFilter(request, response);
	}
	
}
