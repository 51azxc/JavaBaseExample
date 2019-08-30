package design.pattern.structural.bridge;

public abstract class Animal {
    protected Food food;

    public Animal(Food food) {
        this.food = food;
    }

    public abstract void eat();
}

class Cat extends Animal {
    public Cat(Food food) {
        super(food);
    }

    @Override
    public void eat() {
        System.out.println("Cat eat " + food.food());
    }
}

class Dog extends Animal {
    public Dog(Food food) {
        super(food);
    }

    @Override
    public void eat() {
        System.out.println("Dog eat " + food.food());
    }
}


