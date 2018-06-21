package com.example.spring.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;


@Aspect
public class HelloAspect {
	
	@Pointcut(value="execution(* sayHello(..)) && args(s1,s2)")
	public void helloService(String s1,String s2){}
	
	@Before(value="helloService(s1,s2)")
	public void beforeMethod(String s1,String s2){
		System.out.println("before:"+s1+" "+s2);
	}
	
	@Around("helloService(s1,s2)")
	public Object aroundMethod(ProceedingJoinPoint pjp,String s1,String s2) throws Throwable{
		System.out.println("around before: "+s1);
		Object obj = pjp.proceed();
		System.out.println("around after: "+s2);
		return obj;
	}
	
	@AfterReturning(value="helloService(s1,s2)",returning="rtv")
	public void afterMethod(JoinPoint jp,Object rtv,String s1,String s2){
		Object[] obj = jp.getArgs();
		for(Object o:obj){
			System.out.println(o.toString());
		}
		System.out.println(jp.getSignature().getDeclaringTypeName()+"."+jp.getSignature().getName()+" return: "+rtv);
	}
}
