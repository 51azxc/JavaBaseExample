package com.example.servlet.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestServletByMockito {
	private AlphabetServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void setUp() throws Exception {
		servlet = new AlphabetServlet();
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
	}

	@Test
	public void testList() throws Exception {
		when(request.getParameter("para")).thenReturn("list");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		when(response.getWriter()).thenReturn(pw);
		servlet.doPost(request, response);
		String result = sw.getBuffer().toString().trim();
		Assert.assertTrue("contains", result.contains("A"));
		System.out.println("result: " + result);
	}

	@Test
	public void testMap() throws Exception {
		when(request.getParameter("para")).thenReturn("map");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		when(response.getWriter()).thenReturn(pw);
		servlet.doPost(request, response);
		String result = sw.getBuffer().toString().trim();
		Assert.assertThat(result, CoreMatchers.containsString("\"65\":\"A\""));
		System.out.println("result: " + result);
	}

	@Test
	public void testLogin() throws Exception {
		HttpSession session = mock(HttpSession.class);
		when(request.getParameter("username")).thenReturn("admin");
		when(request.getParameter("password")).thenReturn("12345");
		when(request.getSession()).thenReturn(session);
		new LoginServlet().doPost(request, response);
		verify(session).setAttribute("username", "admin");
		verify(response).sendRedirect("welcome.jsp");
	}
}
