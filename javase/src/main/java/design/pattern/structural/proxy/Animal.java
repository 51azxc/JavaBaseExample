package design.pattern.structural.proxy;

public interface Animal {
    void operation();
}

class Cat implements Animal {
    private String name;

    public Cat(String name) {
        this.name = name;
    }

    @Override
    public void operation() {
        System.out.print("I'm a " + name + ", I can catch mice");
    }
}

class Dog implements Animal {
    private String name;
    private Animal cat;
    public Dog(String name) {
        this.name = name;
    }

    @Override
    public void operation() {
        if (cat == null) {
            cat = new Cat(name);
        }
        cat.operation();
        System.out.println(" too");
    }
}
