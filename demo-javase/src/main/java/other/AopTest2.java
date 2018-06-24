package other;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * 通过动态代理简单实现AOP
 */

public class AopTest2 {

	public static void main(String[] args) {
		IHello2 hello = (IHello2) new HelloProxy2().bind(new LogOperation(),new Hello2());
		hello.sayGoodBye("aaa");
		hello.sayHello("aaa");
	}

}

interface IHello2{
	public void  sayHello(String name);
	public void sayGoodBye(String name);
}

class Hello2 implements IHello2{
	public void sayHello(String name) {
		System.out.println("Hello "+name);
	}
	public void sayGoodBye(String name) {
		System.out.println("GoodBye: "+name);
	}
}

interface IOperation{
	public void beforeMethod(Method method);
	public void afterMethod(Method method);
}

class LogOperation implements IOperation{
	public void beforeMethod(Method method) {
		System.out.println("before "+method.getName());
	}
	public void afterMethod(Method method) {
		System.out.println("after "+method.getName());
	}
}

class HelloProxy2 implements InvocationHandler{
	//操作者
	private Object proxy;
	//处理对象(例子中的Hello)
	private Object delegate;
	
	//动态生成方法被处理过后的对象 (写法固定)
	public Object bind(Object proxy,Object delegate){
		this.proxy = proxy;
		this.delegate = delegate;
		return Proxy.newProxyInstance(this.delegate.getClass().getClassLoader(), 
				this.delegate.getClass().getInterfaces(), this);
	}
	//要处理的对象中的每个方法会被此方法送去JVM调用,也就是说,要处理的对象的方法只能通过此方法调用
	//此方法是动态的,不是手动调用的
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		try{
			//反射得到操作者的实例
			Class<? extends Object> clazz = this.proxy.getClass();
			//反射得到操作者的Start方法
			Method m1 = clazz.getDeclaredMethod("beforeMethod", new Class[]{Method.class});
			//反射执行start方法
			m1.invoke(this.proxy, new Object[]{method});
			//执行要处理对象的原本方法
			result = method.invoke(this.delegate, args);
			//反射得到操作者的end方法
			Method m2 = clazz.getDeclaredMethod("afterMethod", new Class[]{Method.class});
			//反射执行end方法
			m2.invoke(this.proxy, new Object[]{method});
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
