package com.alphabet;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.test.servlet.AlphabetServlet;
import com.test.servlet.LoginServlet;

public class TestServletByEasyMock {

	private AlphabetServlet alphabetServlet;
	private HttpServletRequest mockRequest;
	private HttpServletResponse mockResponse;
	
	private LoginServlet loginServlet;
	
	@Before
	public void setUp() throws Exception {
		alphabetServlet = new AlphabetServlet();
		mockRequest = EasyMock.createMock(HttpServletRequest.class);
		mockResponse = EasyMock.createMock(HttpServletResponse.class);
		
		loginServlet = new LoginServlet();
	}
	
	@After
	public void tearDown() throws Exception {
		//为了验证指定的调用行为确实发生了，要调用verify(mock)进行验证
		EasyMock.verify(mockRequest, mockResponse);
	}
	
	@Test
	public void testServlet() throws Exception {
		expect(mockRequest.getParameter("para")).andReturn("list");
		mockResponse.setContentType("text/html;charset=utf-8");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		expect(mockResponse.getWriter()).andReturn(pw);
		expectLastCall();
		replay(mockRequest, mockResponse);
		alphabetServlet.doPost(mockRequest, mockResponse);
		String result = sw.getBuffer().toString().trim();
		System.out.println("result: "+result);
	}
	
	@Test
	public void testLoginServelt() throws Exception {
		mockRequest.getParameter("username");
		expectLastCall().andReturn("admin");
		mockRequest.getParameter("password");
		expectLastCall().andReturn("1245");
		
		mockResponse.sendRedirect("login.jsp");
		
		replay(mockRequest, mockResponse);
		loginServlet.doPost(mockRequest, mockResponse);
	}
	
}
