package design.pattern.structural.facade;

public class AnimalFacade {
    private Animal dog;
    private Animal cat;

    public AnimalFacade() {
        this.dog = new Dog();
        this.cat = new Cat();
    }

    public void dogBark() {
        dog.bark();
    }

    public void catBark() {
        cat.bark();
    }
}
