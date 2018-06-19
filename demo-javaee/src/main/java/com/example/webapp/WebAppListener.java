package com.example.webapp;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServlet;

/*
 * Servlet3.0 监听器
 * 
 * @WebListener用于将类声明为监听器，被`@WebListener`标注的类必须实现以下至少一个接口
 * 1. ServletContextListener 
 * 2. ServletContextAttributeListener 
 * 3. ServletRequestListener 
 * 4. ServletRequestAttributeListener 
 * 5. HttpSessionListener 
 * 6. HttpSessionAttributeListener
 * 该注解只有一个属性value用于描述监听器的信息。可选
 */
@WebListener(value = "listener")
public class WebAppListener extends HttpServlet implements ServletContextListener {

	private static final long serialVersionUID = 1L;
	
	class MyTask extends TimerTask {
		@Override
		public void run() {
			System.out.println("Hello World");
		}
	}
	
	private Timer timer = null;
	private MyTask myTask = null;
	
    public void contextDestroyed(ServletContextEvent arg0)  { 
    	//终止定时器
    	myTask.cancel();	
    }
    public void contextInitialized(ServletContextEvent arg0)  { 
    	if(myTask==null){
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.HOUR, 12);
    		myTask = new MyTask();
    		timer = new Timer();
    		// 如果指定开始执行的时间在当前系统运行时间之前
    		// schedule不会将过去的时间算上
    		// scheduleAtFixedRate会算过去的时间也作为周期执行
    		timer.schedule(myTask, cal.getTime());
    	}
    }

}
