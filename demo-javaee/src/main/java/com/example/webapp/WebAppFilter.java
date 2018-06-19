package com.example.webapp;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Servlet3.0 过滤器
 * @WebFilter用于将一个类声明为过滤器，该注解将会在部署时被容器处理，容器将根据具体的属性配置将相应的类部署为过滤器。
 * 注解里所有属性均为可选属性，但是value/urlPatterns/ervletNames三者必需至少包含一个，且value和urlPatterns不能共存
 */
@WebFilter(
  // 指定过滤器的name属性，等价于<filter-name>
  filterName = "filter",
  // 指定一组过滤器的URL匹配模式。等价于<url-pattern>标签
  urlPatterns={"/*"},
  // 等价于urlPatterns属性。两个属性不能同时使用,
  // 如果同时指定，通常忽略value的取值
  //value = {"/*"},
  // 指定过滤器将应用于哪些Servlet。
  // 取值是@WebServlet中的name属性的取值，
  // 或者是web.xml中<servlet-name>的取值
  servletNames = {"AsyncServlet"},
  //指定过滤器的转发模式。具体取值包括ASYNC/ERROR/FORWARD/INCLUDE/REQUEST
  dispatcherTypes = DispatcherType.FORWARD,
  // 声明过滤器是否支持异步操作模式，等价于`<async-supported>`标签
  asyncSupported = false,
  // 该过滤器的描述信息，等价于<description>标签
  description = "filter",
  // 该过滤器的显示名，通常配合工具使用，等价于<display-name>标签
  displayName = "filter",
  //指定一组Servlet初始化参数，等价于<init-param>标签
  initParams = @WebInitParam(name = "username", value = "tom")
)
public class WebAppFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		// 不过滤的uri  
		String[] notFilter = new String[]{"log","resources"};
		// 请求的uri  
		String uri = req.getRequestURI();
		// 是否过滤  
		boolean doFilter = true;
		for(String s : notFilter){
			if(uri.indexOf(s) != -1){
				// 如果uri中包含不过滤的uri，则不进行过滤  
				doFilter = false;
				break;
			}
		}
		if(doFilter){
			// 执行过滤 
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write("no auth");
		}else{
			//放行
			chain.doFilter(request, response);
		}
	}

}
