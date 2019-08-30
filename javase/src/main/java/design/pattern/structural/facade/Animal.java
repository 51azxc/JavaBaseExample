package design.pattern.structural.facade;

public interface Animal {
    void bark();
}

class Cat implements Animal {
    @Override
    public void bark() {
        System.out.println("I'm a cat, meow!");
    }
}

class Dog implements Animal {
    @Override
    public void bark() {
        System.out.println("I'm a dog, woof!");
    }
}
