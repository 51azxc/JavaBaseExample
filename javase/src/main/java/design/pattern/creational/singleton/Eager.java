package design.pattern.creational.singleton;

//饿汉模式
public class Eager {
	//jvm保证任何线程访问此静态变量之前一定创建实例
	private static Eager eager = new Eager();
	//默认构造函数，外界无法对其初始化
	private Eager() {}
	//返回单例
	public static Eager getInstance() {
		return eager;
	}
}
