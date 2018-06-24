package other;

class A {
	public A() {
		System.out.println("ClassA");
	}
	{ System.out.println("I'm A"); }
	static {
		System.out.println("staticA");
	}
}

public class B extends A {

	public B() {
		System.out.println("ClassB");
	}
	{ System.out.println("I'm B"); }
	static {
		System.out.println("staticB");
	}
	
	public static void main(String[] args) {
		System.out.println("start");
		new B();
		new B();
		System.out.println("end");
	}

}
