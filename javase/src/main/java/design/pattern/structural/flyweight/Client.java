package design.pattern.structural.flyweight;

/*
享元模式

意图：
运用共享技术有效地支持大量细粒度的对象。

适用性：
一个应用程序使用了大量的对象。
完全由于使用大量的对象，造成很大的存储开销。
对象的大多数状态都可变为外部状态。
如果删除对象的外部状态，那么可以用相对较少的共享对象取代很多组对象。
应用程序不依赖于对象标识。由于Flyweight 对象可以被共享，对于概念上明显有别的对象，标识测试将返回真值。
 */
public class Client {
    public static void main(String[] args) {
        Shape s1 = ShapeFactory.getCircle("green");
        Shape s2 = ShapeFactory.getCircle("red");
        Shape s3 = ShapeFactory.getCircle("red");
        s1.draw();
        s2.draw();
        s3.draw();

        System.out.println(s2 == s3);
    }
}
