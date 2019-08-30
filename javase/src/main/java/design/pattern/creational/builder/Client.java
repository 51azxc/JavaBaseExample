package design.pattern.creational.builder;

/*
建造者

意图：
将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。

适用性：
当创建复杂对象的算法应该独立于该对象的组成部分以及它们的装配方式时。
当构造过程必须允许被构造的对象有不同的表示时。

examples:
java.lang.StringBuffer
Apache Camel builders
 */
public class Client {
    public static void main(String[] args) {
        Computer basic = new Computer.Builder("i5-8400", "MSI-B450M", 8, 1024).build();
        System.out.println(basic.toString());
        Computer gamer = new Computer.Builder("i5-8400", "MSI-B450M", 8, 1024)
                .withGPU("RTX2080").withRAM(8).withSSD(256).withHDD(2048).build();
        System.out.println(gamer.toString());
    }
}
