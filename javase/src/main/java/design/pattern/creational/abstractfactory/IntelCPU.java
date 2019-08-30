package design.pattern.creational.abstractfactory;

public interface IntelCPU {
    String model();
}

class I5CPU implements IntelCPU {
    @Override
    public String model() {
        return "Intel i5-8400";
    }
}

class I7CPU implements IntelCPU {
    @Override
    public String model() {
        return "Intel i7-8700";
    }
}
