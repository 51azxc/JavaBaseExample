package com.example.spring.mvc.interceptor.aspect;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class MyAspect {
	
	@Pointcut("execution(* com.example.spring.mvc.interceptor.controller.MyController.test(..))")
	private void aroundMethod(){}
	
	@Around(value="aroundMethod()")
	public Object aroundAdvice(final ProceedingJoinPoint pjp) throws Throwable{
		Object responseBody = pjp.proceed();
		
		System.out.println("around: "+responseBody.toString());
		for(Object o:pjp.getArgs()){
			if( o instanceof HttpServletResponse){
				HttpServletResponse response = (HttpServletResponse) o;
				response.sendRedirect("/test1");
			}
		}
		
		return null;
	}
}
