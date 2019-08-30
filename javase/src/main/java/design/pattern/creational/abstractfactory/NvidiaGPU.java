package design.pattern.creational.abstractfactory;

public interface NvidiaGPU {
    String model();
}

class GTX1070 implements NvidiaGPU {
    @Override
    public String model() {
        return "Nvidia GTX1070";
    }
}

class GTX1060 implements NvidiaGPU {
    @Override
    public String model() {
        return "Nvidia GTX1060";
    }
}
