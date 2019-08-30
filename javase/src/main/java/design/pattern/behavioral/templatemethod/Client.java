package design.pattern.behavioral.templatemethod;


/*
模板方法

意图：
定义一个操作中的算法的骨架，而将一些步骤延迟到子类中。TemplateMethod 使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤。

适用性：
一次性实现一个算法的不变的部分，并将可变的行为留给子类来实现。
各子类中公共的行为应被提取出来并集中到一个公共父类中以避免代码重复。
 */
public class Client {
    public static void main(String[] args) {
        Game game1 = new Football();
        game1.play();
        Game game2 = new Basketball();
        game2.play();
    }
}
