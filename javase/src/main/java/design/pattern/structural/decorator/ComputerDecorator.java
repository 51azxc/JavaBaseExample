package design.pattern.structural.decorator;

public abstract class ComputerDecorator implements Computer {
    protected Computer computer;

    public ComputerDecorator(Computer computer) {
        this.computer = computer;
    }

    @Override
    public void operation() {
        this.computer.operation();
    }
}

class GamingComputer extends ComputerDecorator {
    public GamingComputer(Computer computer) {
        super(computer);
    }

    @Override
    public void operation() {
        super.operation();
        System.out.println(" with a gaming graphics");
    }
}