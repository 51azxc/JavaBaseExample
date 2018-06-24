package other;

/*
 * 通过代理模式实现AOP
 */

public class AopTest1 {

	public static void main(String[] args) {
		IHello1 hello1 = new HelloProxy1(new Hello1());
		hello1.sayHello("aaa");
		System.out.println("---------------------");
		IHello1 hello2 = new Hello1();
		hello2.sayHello("bbb");
	}

}

interface IHello1 {
	public void sayHello(String name);
}

class Hello1 implements IHello1 {
	@Override
	public void sayHello(String name) {
		System.out.println("Hello " + name);
	}
}

class HelloProxy1 implements IHello1 {
	private IHello1 hello;

	public HelloProxy1(IHello1 hello) {
		this.hello = hello;
	}

	@Override
	public void sayHello(String name) {
		System.out.println("before");
		hello.sayHello(name);
		System.out.println("after");
	}
}
