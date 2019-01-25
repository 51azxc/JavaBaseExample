package com.example.servlet;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.service.ImageCode;

/*
 * 发送图片验证码
 * 
 * jsp部分
 * <form action="<%=request.getContextPath()%>/CodeServlet" method="post">
 *   <input type="text" id="code" name="code">
 *   <img id="image" src="<%=request.getContextPath()%>/CodeServlet" title="点击更换图片" onclick="refresh()">
 *   <input type="submit" id="btn" value="submit">
 * </form>
 * <script type="text/javascript">
 *   function refresh(){
 *     //添加日期用于刷新图片
 *	   document.getElementById("image").src="<%=request.getContextPath()%>/CodeServlet?"+new Date();
 *   }
 * </script>
 */

@WebServlet("/ImageCodeServlet")
public class ImageCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//设置不缓存图片
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "No-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/ipeg");
		ImageCode ic = new ImageCode();
		BufferedImage bi = new BufferedImage(ic.getWidth(), ic.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		//定义字体
		Font f = new Font("Times New Roman", Font.BOLD, 18);
		g.setFont(f);
		g.setColor(ic.getColor(123, 222));
		//绘制背景
		g.fillRect(0, 0, ic.getWidth(), ic.getHeight());
		g.setColor(ic.getColor(11, 111));
		ic.drawLine(g, 24);
		String code = ic.drawString(g, 4);
		System.out.println("code: "+code);
		//将验证码存入session中
		HttpSession session = request.getSession();
		session.setAttribute("code", code);
		g.dispose();
		ImageIO.write(bi, "JPEG", response.getOutputStream());
		response.getOutputStream().flush();    
		response.getOutputStream().close();    
		response.flushBuffer();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String code = request.getParameter("code");
		String checkCode = (String) request.getSession().getAttribute("code");
		if(code.equals(checkCode)){
			System.out.println("same");
		}
		request.getRequestDispatcher("index.jsp").forward(request, response);
	}

}
