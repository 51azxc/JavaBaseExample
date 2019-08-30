package design.pattern.structural.decorator;

/*
装饰器模式

意图：
动态地给一个对象添加一些额外的职责。就增加功能来说，Decorator 模式相比生成子类更为灵活。

适用性：
在不影响其他对象的情况下，以动态、透明的方式给单个对象添加职责。
处理那些可以撤消的职责。
当不能采用生成子类的方法进行扩充时。
一种情况是，可能有大量独立的扩展，为支持每一种组合将产生大量的子类，使得子类数目呈爆炸性增长。
另一种情况可能是因为类定义被隐藏，或类定义不能用于生成子类。

examples:
java.io.InputStream
java.io.OutputStream
java.io.Reader
java.io.Writer
java.util.Collections#synchronizedXXX()
java.util.Collections#unmodifiableXXX()
java.util.Collections#checkedXXX()
 */
public class Client {
    public static void main(String[] args) {
        Computer c1 = new BasicComputer();
        c1.operation();
        System.out.println();
        Computer c2 = new GamingComputer(new BasicComputer());
        c2.operation();
    }
}
