package design.pattern.structural.bridge;

public interface Food {
    String food();
}

class Fish implements Food {
    @Override
    public String food() {
        return "fish";
    }
}

class Meat implements Food {
    @Override
    public String food() {
        return "meat";
    }
}
