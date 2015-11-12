package com.test.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		System.out.println("username: "+username+"\t password: "+password);
		if("admin".equals(username) && "12345".equals(password)){
			req.getSession().setAttribute("username", username);
			resp.sendRedirect("welcome.jsp");
		}else{
			resp.sendRedirect("login.jsp");
		}
	}

}
