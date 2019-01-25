package com.example.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 操作Cookie
 * 
 * cookies是一种WEB服务器通过 浏览器在访问者的硬盘上存储信息的手段,
 * 当用户再次访问某个站点时，服务端将要求浏览器查找并返回先前发送的Cookie信息，来识别这个用户。
 * cookie中的名字和值都不能包含空白字符以及下列字符：`@ : ;? , " / [ ] ( ) = `
 */
@WebServlet("/cookie")
public class CookieServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 获取cookie
		Cookie cs[] = req.getCookies();
		for (int i = 0, len = cs.length - 1; i < len; i++) {
			Cookie cc = cs[i];
			System.out.println(cc.getName() + ":" + cc.getValue());
		}
		// 存入cookie
		Cookie c = new Cookie("userName", "a");
		// 设置存活时间0为删除，负数为浏览器关闭就删除,单位为秒
		c.setMaxAge(-1);
		resp.addCookie(c);
	}

}
