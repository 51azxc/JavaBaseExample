package design.pattern.structural.composite;

/*
组合模式

意图：
将对象组合成树形结构以表示“部分-整体”的层次结构。Composite使得用户对单个对象和组合对象的使用具有一致性。

适用性：
你想表示对象的部分-整体层次结构。
你希望用户忽略组合对象与单个对象的不同，用户将统一地使用组合结构中的所有对象。

examples:
java.awt.Container
java.awt.Component
 */
public class Client {
    public static void main(String[] args) {
        Employee e1 = new Employee("a", 5000, "boss");
        Employee e2 = new Employee("b", 4000, "manager");
        Employee e3 = new Employee("c", 4000, "manager");
        Employee e4 = new Employee("d", 3000, "programmer");
        Employee e5 = new Employee("e", 3000, "programmer");
        Employee e6 = new Employee("f", 3000, "programmer");
        Employee e7 = new Employee("g", 3000, "programmer");

        e2.addSubordinate(e4);
        e2.addSubordinate(e5);
        e3.addSubordinate(e6);
        e3.addSubordinate(e7);
        e1.addSubordinate(e2);
        e1.addSubordinate(e3);

        System.out.println(e1);
        e1.getSubordinates().forEach(employee -> {
            System.out.println(employee);
            employee.getSubordinates().forEach(System.out::println);
        });
    }
}
