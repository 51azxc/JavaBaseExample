package com.example.servlet.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.service.AlphabetService;
import com.google.gson.Gson;

@WebServlet("/AlphabetServlet")
public class AlphabetServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Gson gson;
	private AlphabetService alphabetService;
	
	public AlphabetServlet() {
		gson = new Gson();
		alphabetService = new AlphabetService();
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		doPost(req, resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String para = req.getParameter("para");
		String result;
		if ("map".equals(para)) {
			result = gson.toJson(alphabetService.getAlphabetMap());
		}else if("list".equals(para)){
			result = gson.toJson(alphabetService.getAlphabetList());
		}else{
			result = "";
		}
		//resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=utf-8");
		resp.getWriter().write(result);
	}
}

