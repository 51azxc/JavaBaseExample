package design.pattern.creational.factorymethod;

public abstract class ComputerFactory {
    protected abstract Computer factoryMethod();
    public void anOperation() {
        System.out.println(factoryMethod().name() + " Computer");
    }
}

class DellComputerFactory extends ComputerFactory {
    @Override
    protected Computer factoryMethod() {
        return new DELL();
    }
}

class HpComputerFactory extends ComputerFactory {
    @Override
    protected Computer factoryMethod() {
        return new HP();
    }
}

class LenovoComputerFactory extends ComputerFactory {
    @Override
    protected Computer factoryMethod() {
        return new Lenovo();
    }
}

class AsusComputerFactory extends ComputerFactory {
    @Override
    protected Computer factoryMethod() {
        return new ASUS();
    }
}
