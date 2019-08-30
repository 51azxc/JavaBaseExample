package design.pattern.structural.decorator;

public interface Computer {
    void operation();
}

class BasicComputer implements Computer {
    @Override
    public void operation() {
        System.out.print("This is a computer");
    }
}

