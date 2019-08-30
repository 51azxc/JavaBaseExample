package design.pattern.structural.flyweight;

public interface Shape {
    void draw();
}

class Circle implements Shape {
    private String color;

    public Circle(String color) {
        this.color = color;
    }

    @Override
    public void draw() {
        System.out.println("draw a circle with " + color + " color");
    }
}
