package other;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * 通过代理模式及动态代理实现AOP
 */

public class AopTest {

	public static void main(String[] args) {
		System.out.println("----------static proxy----------");
		staticProxyExample();
		System.out.println("----------dynamic proxy----------");
		dynamicProxyExample();
	}
	
	//通过代理模式实现AOP
	public static void staticProxyExample() {
		//代理类
		class HelloProxy implements Hello {
			private Hello hello;

			public HelloProxy(Hello hello) {
				this.hello = hello;
			}

			@Override
			public void sayHello(String name) {
				System.out.println("before say hello");
				hello.sayHello(name);
				System.out.println("after say hello");
			}

			@Override
			public void sayGoodBye(String name) {
				System.out.println("before say bye");
				hello.sayGoodBye(name);
				System.out.println("after say bye");
			}
		}
		
		Hello hello = new HelloProxy(new HelloImpl());
		hello.sayHello("static");
		hello.sayGoodBye("static");
	}
	
	public static void dynamicProxyExample() {
		class LogOperation implements Operation{
			public void beforeMethod(Method method) {
				System.out.println("before "+method.getName());
			}
			public void afterMethod(Method method) {
				System.out.println("after "+method.getName());
			}
		}
		
		//动态调用
		class HelloProxy implements InvocationHandler{
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
		
		Hello hello = (Hello) new HelloProxy().bind(new LogOperation(), new HelloImpl());
		hello.sayHello("dynamic");
		hello.sayGoodBye("dynamic");
	}
	
	//静态内部类，方便直接调用
	static class HelloImpl implements Hello {
		@Override
		public void sayHello(String name) {
			System.out.println("Hello " + name);
		}
		@Override
		public void sayGoodBye(String name) {
			System.out.println("Bye " + name);
		}
	}
	
	interface Hello {
		public void sayHello(String name);
		public void sayGoodBye(String name);
	}
	
	interface Operation{
		public void beforeMethod(Method method);
		public void afterMethod(Method method);
	}
}