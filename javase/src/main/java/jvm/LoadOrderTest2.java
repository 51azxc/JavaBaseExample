package jvm;

public class LoadOrderTest2 {

	//执行main方法时触发了初始化静态变量操作
	public static void main(String[] args) {
		test();
	}
	//初始化类时，最先初始化静态变量，因此会初始化test这个静态变量
	//一旦完成，就不会再做静态初始化，因此实例初始化放在了静态初始化之前
	//所以静态块到后边才调用，而静态变量b只能赋初始值0
	static LoadOrderTest2 test = new LoadOrderTest2();
	
	static {
		System.out.println(1);
	}
	
	{ System.out.println(2); }
	
	public LoadOrderTest2() {
		System.out.println(3);
		System.out.println("a= "+a+",b= "+b);
	}
	
	public static void test() {
		System.out.println(4);
	}
	
	int a = 10;
	static int b = 11;
}
