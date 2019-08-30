package design.pattern.behavioral.iterator;

import java.util.Random;

/*
迭代器模式

意图：
提供一种方法顺序访问一个聚合对象中各个元素, 而又不需暴露该对象的内部表示。

适用性：
访问一个聚合对象的内容而无需暴露它的内部表示。
支持对聚合对象的多种遍历。
为遍历不同的聚合结构提供一个统一的接口(即, 支持多态迭代)。

examples:
java.util.Iterator
java.util.Enumeration
 */
public class Client {
    public static void main(String[] args) {
        Integer[] array = new Random().ints(10,10, 100).boxed().toArray(Integer[]::new);
        CustomList<Integer> list = new IntegerList(array);
        CustomIterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + "\t");
        }
    }
}
