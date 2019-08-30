package design.pattern.structural.composite;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String name;
    private int salary;
    private String position;
    private List<Employee> subordinates;

    public Employee(String name, int salary, String position) {
        this.name = name;
        this.salary = salary;
        this.position = position;
        this.subordinates = new ArrayList<>();
    }

    public void addSubordinate(Employee e) {
        this.subordinates.add(e);
    }

    public void removeSuboridinate(Employee e) {
        this.subordinates.remove(e);
    }

    public List<Employee> getSubordinates() {
        return subordinates;
    }

    @Override
    public String toString() {
        return "Employee: " + name + " position: " + position + " salary: " + salary;
    }
}
