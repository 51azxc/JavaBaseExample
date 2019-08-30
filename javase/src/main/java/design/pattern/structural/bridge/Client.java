package design.pattern.structural.bridge;

/*
桥接模式

意图：
将抽象部分与它的实现部分分离，使它们都可以独立地变化。

适用性：
你不希望在抽象和它的实现部分之间有一个固定的绑定关系。
 */
public class Client {
    public static void main(String[] args){
        Animal cat = new Cat(new Fish());
        Animal dog = new Dog(new Meat());
        cat.eat();
        dog.eat();
    }
}
