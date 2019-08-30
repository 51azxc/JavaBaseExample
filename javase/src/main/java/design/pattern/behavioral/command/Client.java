package design.pattern.behavioral.command;

/*
命令模式

意图：
将一个请求封装为一个对象，从而使你可用不同的请求对客户进行参数化；对请求排队或记录请求日志，以及支持可撤消的操作。

examples:
java.lang.Runnable
org.junit.runners.model.Statement
Netflix Hystrix
javax.swing.Action
 */
public class Client {
    public static void main(String[] args) {
        Light light = new Light();
        Command turnOn = new LightTurnOn(light);
        Command turnOff = new LightTurnOff(light);
        Room room = new Room();
        room.setCommand(turnOn);
        room.setCommand(turnOff);
        room.excute();
    }
}
