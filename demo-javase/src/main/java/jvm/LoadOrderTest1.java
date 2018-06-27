package jvm;

/*
 * 类的实例化顺序
 * 
 * 父类优先于子类实例化
 * 
 * 父类静态代码块 - 子类静态代码块 - 父类代码块 - 父类构造函数 - 子类代码块 - 子类构造函数
 * 静态代码块只会执行一次
 */

public class LoadOrderTest1 extends LoadOrderTest1SuperClass {
	public LoadOrderTest1() {
		System.out.println("subclass construction");
	}
	{ System.out.println("subclass code"); }
	static {
		System.out.println("subclass static code");
	}
	
	public static void main(String[] args) {
		new LoadOrderTest1();
		new LoadOrderTest1();
	}
}

class LoadOrderTest1SuperClass {
	public LoadOrderTest1SuperClass() {
		System.out.println("superclass construction");
	}
	{ System.out.println("superclass code"); }
	static {
		System.out.println("superclass static code");
	}
}