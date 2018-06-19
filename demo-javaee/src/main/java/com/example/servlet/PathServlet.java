package com.example.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 获取路径
 * jsp获取方法
 * <%=request.getContextPath() %>
 * <%=request.getServletPath() %>
 * <%=request.getRequestURI() %>
 * <%=request.getRequestURL().toString() %>
 */
@WebServlet("/path")
public class PathServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
    
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//获取当前项目名
		System.out.println(req.getContextPath());
		//获取到当前Servlet相对路径
		System.out.println(req.getRequestURI());
		//获取浏览器地址栏的路径
		System.out.println(req.getRequestURL());
		//获取当前Servlet的访问路径
		System.out.println(req.getServletPath());
		//获取服务器项目实际路径
		System.out.println(req.getSession().getServletContext().getRealPath(""));
		
		//通过ServletContext获取资源文件
		ServletContext context = this.getServletContext();
		context.getResourceAsStream("/WEB-INF/classes/log4j.properties");
		URL url = context.getResource("/WEB-INF/classes/log4j.properties");
		System.out.println(url.getPath());
		System.out.println(url.getFile());
		
	}
}
