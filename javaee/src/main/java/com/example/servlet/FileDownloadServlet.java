package com.example.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 使用Servlet完成文件下载功能
 */

@WebServlet("/download")
public class FileDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileName = "test.txt";
		response.setContentType("text/plain");
		response.setHeader("Location", fileName);
		response.setHeader("Content-Desposition", "attachment; filename="+fileName);
		OutputStream os = response.getOutputStream();
		InputStream is = new FileInputStream(fileName);
		byte[] buffer = new byte[1024];
		int len;
		while((len = is.read(buffer))!=-1){
			os.write(buffer, 0, len);
		}
		os.flush();
		os.close();
		is.close();
	}
}
