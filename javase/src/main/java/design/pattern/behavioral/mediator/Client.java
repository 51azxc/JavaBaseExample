package design.pattern.behavioral.mediator;

/*
中介者模式

意图：
用一个中介对象来封装一系列的对象交互。中介者使各对象不需要显式地相互引用，从而使其耦合松散，而且可以独立地改变它们之间的交互。

适用性：
一组对象以定义良好但是复杂的方式进行通信。产生的相互依赖关系结构混乱且难以理解。
一个对象引用其他很多对象并且直接与这些对象通信,导致难以复用该对象。
想定制一个分布在多个类中的行为，而又不想生成太多的子类。

examples:
java.util.Timer.scheduleXXX()
java.util.concurrent.Executor#execute()
java.util.concurrent.ExecutorService.submit()/invokeXXX()
java.util.concurrent.ScheduledExecutorService.scheduleXXX()
java.lang.reflect.Method#invoke()
 */
public class Client {
    public static void main(String[] args) {
        ChatUser u1 = new ChatUser("a");
        ChatUser u2 = new ChatUser("b");
        u1.sendMessage("Hello");
        u2.sendMessage("Hi");
        u1.sendMessage("How are you?");
        u2.sendMessage("I'm fine thank you! and you>");
        u1.sendMessage("I'm fine too");
        u2.sendMessage("Where are you from?");
        u1.sendMessage("I'm from China");
        u2.sendMessage("那你装什么逼");
    }
}
