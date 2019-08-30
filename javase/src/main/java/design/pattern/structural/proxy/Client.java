package design.pattern.structural.proxy;

/*
代理模式

意图：
为其他对象提供一种代理以控制对这个对象的访问。

examples:
java.lang.reflect.Proxy
Apache Commons Proxy/Mockito/EasyMock
 */
public class Client {
    public static void main(String[] args) {
        Animal cat = new Cat("cat");
        cat.operation();
        System.out.println();
        Animal dog = new Dog("dog");
        dog.operation();
    }
}
