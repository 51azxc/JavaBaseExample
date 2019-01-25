package other;

public class InnerClassTest {

	public static void main(String[] args) {
		Outer1 o1 = new Outer1();
		Outer1.Inner1 i1 = o1.new Inner1();
		i1.print("i1");
		
		Outer2 o2 = new Outer2();
		o2.print("i2");
		
		Outer3.Inner3 i3 = new Outer3.Inner3();
		i3.print();
		
		Outer4 o4 = new Outer4();
		Inner4 i4 = o4.getInner4("i4", 1);
		System.out.println(i4.getResult());
		
		Inner4 i4_1 = o4.getInner4_1("a", 22);
		System.out.println(i4_1.getResult());
		
		subInner1 si1 = new subInner1(o1);
		si1.print("ssss");
	}

}

/*
 * 成员内部类，就是作为外部类的成员，可以直接使用外部类的所有成员和方法，即使是private的。
 * 同时外部类要访问内部类的所有成员变量/方法，则需要通过内部类的对象来获取。
 * 要注意的是，成员内部类不能含有static的变量和方法。因为成员内部类需要先创建了外部类，才能创建它自己的。
 * 在成员内部类要引用外部类对象时，使用outer.this来表示外部类对象
 */
class Outer1{
	class Inner1{
		public void print(String str){
			System.out.println("Inner1 print "+str);
		}
	}
}

/*
 * 方法内部类
 * 是指内部类定义在方法和作用域内,也像别的类一样进行编译，但只是作用域不同而已，只在该方法或条件的作用域内才能使用，退出这些作用域后无法引用的
 */
class Outer2{
	public void print(String str){
		class Inner2{
			public void print(String str){
				System.out.println("Inner2 print "+str);
			}
		}
		Inner2 i2 = new Inner2();
		i2.print(str);
	}
}

/*
 * 静态内部类
 * 也为嵌套内部类,声明为static的内部类，不需要内部类对象和外部类对象之间的联系，
 * 就是说我们可以直接引用outer.inner，即不需要创建外部类，也不需要创建内部类。
 * 嵌套类和普通的内部类还有一个区别：普通内部类不能有static数据和static属性，
 * 也不能包含嵌套类，但嵌套类可以。而嵌套类不能声明为private，一般声明为public，方便调用
 */
class Outer3{
	public static class Inner3{
		public void print(){
			System.out.println("Inner3 print");
		}
	}
}

/*
 * 匿名内部类
 * 匿名内部类是不能加访问修饰符的。要注意的是，new 匿名类，这个类是要先定义的。
 * **当所在的方法的形参需要被内部类里面使用时，该形参必须为`final`**。
 * **即为拷贝引用，为了避免引用值发生改变，例如被外部类的方法修改等，而导致内部类得到的值不一致，于是用final来让该引用不可改变
 * 如果参数没有在内部类中使用，则无需使用`final`关键字
 */
class Outer4{
	public Inner4 getInner4(final String name,int age){
		return new Inner4() {
			public String getResult() {
				return "Inner4 "+name;
			}
		};
	}
	//匿名内部类通过实例初始化，可以达到类似构造器的效果
	public Inner4 getInner4_1(final String name,final int age){
		return new Inner4() {
			private String studentName;
			private int studentAge;
			//实例初始化
			{
				this.studentName = name;
				this.studentAge = age;
			}
			public String getResult() {
				return this.studentName+" "+this.studentAge;
			}
		};
	}
}
//必须在外部先声明
interface Inner4{
	String getResult();
}

/*
 * 内部类继承
 * 子类的构造函数里面要使用父类的外部类对象.super();而这个对象需要从外面创建并传给形参
 */
class subInner1 extends Outer1.Inner1{
	//一定要加上形参 父类的外部类对象.super()
	public subInner1(Outer1 o1) {
		o1.super();
	}
	
	public void print(String str){
		System.out.println("subInner1: "+str);
	}
}
