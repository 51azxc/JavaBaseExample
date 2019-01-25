package com.example.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/*
 * Servlet3.0使用@MultipartConfig实现文件上传功能
 * 
 * html页面写法
 * <form name="form1" action="fileUpload" method="post" enctype="multipart/form-data">
 *   <input type="file" name="myfile"><br>
 *   <input type="submit" name="submit" value="提交">
 * </form>
 */

@MultipartConfig(
  location="D:/",                 // 文件存放路径
  maxFileSize=1024*1024*1024,     // 上传文件最大值(单位:字节)
  fileSizeThreshold=10*1024*1024, // 当数据量大于该值时，内容将被写入文件
  maxRequestSize=100*1024*1024      // 针对该 multipart/form-data 请求的最大数量，默认值为 -1，表示没有限制
)
@WebServlet("/upload3")
public class FileUpload3Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		//多文件上传
		Collection<Part> parts = request.getParts();
		//单文件上传
		//Part part = request.getPart("file");
		//遍历所有的表单内容，将表单中的文件写入上传文件目录
		for (Iterator<Part> iterator = parts.iterator(); iterator.hasNext();) {  
	        Part part = iterator.next();  
	        //从Part的content-disposition中提取上传文件的文件名  
	        //获取header信息中的content-disposition，如果为文件，则可以从其中提取出文件名  
	        String fileName = part.getHeader("content-disposition");
	        if(fileName!=null){  
	            part.write(fileName);  
	        }  
	    }
	}
}
