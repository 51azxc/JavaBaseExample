package design.pattern.creational.prototype;

import java.util.ArrayList;
import java.util.List;

public class Employees implements Cloneable {
    private List<String> list;
    public Employees() { this.list = new ArrayList<>(); }

    public Employees(List<String> list) { this.list = list; }

    public List<String> getList() { return list; }

    @Override
    protected Object clone() {
        List<String> l = new ArrayList<>();
        list.forEach(s -> l.add(s));
        return new Employees(l);
    }
}
