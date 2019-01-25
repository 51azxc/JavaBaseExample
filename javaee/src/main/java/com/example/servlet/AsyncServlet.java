package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Servlet3.0实现异步处理
 * 通过注解直接指定asyncSupported属性即可。
 * 如果是web.xml需要做如下配置
 * <servlet>  
 *   <servlet-name>xxx</servlet-name>  
 *   <servlet-class>xxx</servlet-class>  
 *   <async-supported>true</async-supported>  
 * </servlet>  
 * <servlet-mapping>  
 *   <servlet-name>xxx</servlet-name>  
 *   <url-pattern>xxx</url-pattern>  
 * </servlet-mapping>
 */

@WebServlet(
  // 指定一组Servlet的URL匹配模式。等价于`<url-pattern>`标签
  urlPatterns = {"/async"},
  /* 以下为非必须参数 */
  // 声明Servlet是否支持异步操作模式，等价于`<async-supported>`标签
  asyncSupported = true,
  // 与urlPatterns一样，两者不能共存
  //value = {"/async"},
  // 指定Servlet的加载顺序，等价于`<load-on-startup>`标签
  loadOnStartup = -1,
  // 指定Servlet的name属性，等价于`<servlet-name>`。
  // 如果没有显式指定，则该Servlet的取值即为类的全限定名
  name = "AsyncServlet",
  // 该Servlet的显示名，通常配合工具使用，等价于<display-name>标签
  displayName = "as",
  // 指定一组Servlet初始化参数，等价于`<init-param>`标签
  initParams = {
    /*
     * 通常不单独使用，而是配合`@WebServlet`或者`@WebFilter`使用。
     * 它的作用是为Servlet或者过滤器指定初始化参数，
     * 这等价于web.xml中`<servlet>`和`<filter>`的`<init-param>`子标签
     */
    @WebInitParam(
      // 指定参数的名字，等价于`<param-name>`。必须。
      name = "username", 
      // 指定参数的值，等价于`<param-value>`。必须。
      value = "tom",
      // 关于参数的描述，等价于`<description>`。可选。
      description = "set username"
    )
  }
)
public class AsyncServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		PrintWriter pw = resp.getWriter();
		pw.write("begin");
		pw.flush();
		// 开始异步调用
		AsyncContext context = req.startAsync();
		// 设置当前异步调用对应的监听器
		context.addListener(new AsyncListener() {
			@Override
			public void onTimeout(AsyncEvent arg0) throws IOException {
				System.out.println("onTimeout");
			}
			@Override
			public void onStartAsync(AsyncEvent arg0) throws IOException {
				System.out.println("onStartAsync");
			}
			@Override
			public void onError(AsyncEvent arg0) throws IOException {
				System.out.println("onError");
			}
			@Override
			public void onComplete(AsyncEvent arg0) throws IOException {
				System.out.println("onComplete");
			}
		});
		// 设置超时时间，当超时之后程序会尝试重新执行异步任务，即我们新起的线程
		context.setTimeout(1000000);
		// 新起线程开始异步调用，start方法不是阻塞式的，
		// 它会新起一个线程来启动Runnable接口，之后主程序会继续执行
		context.start(new Runnable() {
			@Override
			public void run() {
				try{
				    // 等待2秒
					Thread.sleep(2*1000);
					pw.write("async");
					pw.flush();
					// 异步调用完成，如果异步调用完成后不调用complete()方法的话，
					// 异步调用的结果需要等到设置的超时时间过了之后才能返回到客户端
					context.complete();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		pw.write("finish");
		pw.flush();
	}

}
