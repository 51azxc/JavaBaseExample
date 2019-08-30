package design.pattern.creational.abstractfactory;

public interface ComputerFactory {
    IntelCPU getCPU();
    NvidiaGPU getGPU();
}

class DellComputerFactory implements ComputerFactory {
    @Override
    public IntelCPU getCPU() {
        return new I5CPU();
    }

    @Override
    public NvidiaGPU getGPU() {
        return new GTX1070();
    }
}

class HpComputerFactory implements ComputerFactory {
    @Override
    public IntelCPU getCPU() {
        return new I7CPU();
    }

    @Override
    public NvidiaGPU getGPU() {
        return new GTX1060();
    }
}