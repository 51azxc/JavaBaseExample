package design.pattern.creational.factorymethod;

/*
工厂方法

意图：
定义一个用于创建对象的接口，让子类决定实例化哪一个类。Factory Method 使一个类的实例化延迟到其子类。

适用性：
当一个类不知道它所必须创建的对象的类的时候。
当一个类希望由它的子类来指定它所创建的对象的时候。
当类将创建对象的职责委托给多个帮助子类中的某一个，并且你希望将哪一个帮助子类是代理者这一信息局部化的时候。

examples:
java.util.Calendar
java.util.ResourceBundle
java.text.NumberFormat
java.nio.charset.Charset
java.net.URLStreamHandlerFactory
java.util.EnumSet
javax.xml.bind.JAXBContext
 */
public class Client {
    public static void main(String[] args) {
        ComputerFactory dell = new DellComputerFactory();
        ComputerFactory hp = new HpComputerFactory();
        ComputerFactory lenovo = new LenovoComputerFactory();
        ComputerFactory asus = new AsusComputerFactory();
        dell.anOperation();
        hp.anOperation();
        lenovo.anOperation();
        asus.anOperation();
    }
}
