package com.example.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/*
 * Servlet2使用apache commons文件上传
 * 
 * html页面写法
 * <form name="form1" action="fileUpload" method="post" enctype="multipart/form-data">
 *   <input type="file" name="myfile"><br>
 *   <input type="submit" name="submit" value="提交">
 * </form>
 */
@WebServlet("/upload2")
public class FileUpload2Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//临时存放目录
		String tempdir =this.getServletContext().getRealPath("/tmp");
		File tempFile = new File(tempdir);
		if(!tempFile.exists()){
			tempFile.mkdirs();
		}
		//存放目录
	    String savedir =this.getServletContext().getRealPath("/upload");
	    File saveFile = new File(savedir);
	    if(!saveFile.exists()){
	    	saveFile.mkdirs();
	    }
		//检查表弟enctype属性是否为multipart/form-data
		if(ServletFileUpload.isMultipartContent(request)){
			request.setCharacterEncoding("UTF-8");
			DiskFileItemFactory factory = new DiskFileItemFactory();
			//内存最大占用
			factory.setSizeThreshold(1024000);
			//设置缓冲区目录
			factory.setRepository(new File(tempdir));
			ServletFileUpload upload = new ServletFileUpload(factory);
			//单个文件最大值byte
			upload.setFileSizeMax(102400000);
			//所有上传文件的总和最大值byte
			upload.setSizeMax(204800000);
			List<FileItem> items = null;
	        try {
				items = upload.parseRequest(request);
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
	        Iterator<FileItem> it = items.iterator();
	        while(it.hasNext()){
	        	FileItem fileItem = (FileItem) it.next();
	        	//如果是普通字段
	        	if(fileItem.isFormField()){
	        		System.out.println(fileItem.getFieldName());	//获取item的name值
	        		System.out.println(fileItem.getString("UTF-8"));//获取item的value值
	        	}else{
	        		System.out.println(fileItem.getName());	//获取文件名
	        		System.out.println(fileItem.getContentType());  //获取文件类型
	        		System.out.println(fileItem.getSize());  //获取文件大小
	        		if(fileItem.getName()!=null && fileItem.getSize()>0){
	        			File file = new File(savedir, fileItem.getName());
	        			try {
							fileItem.write(file);
						} catch (Exception e) {
							e.printStackTrace();
						}
	        		}
	        	}
	        }
	        
		}
	}

}
