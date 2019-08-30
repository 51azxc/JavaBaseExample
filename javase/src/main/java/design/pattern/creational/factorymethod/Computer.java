package design.pattern.creational.factorymethod;

public interface Computer {
    String name();
}

class DELL implements Computer {
    @Override
    public String name() {
        return "DELL";
    }
}

class HP implements Computer {
    @Override
    public String name() {
        return "HP";
    }
}

class Lenovo implements Computer {
    @Override
    public String name() {
        return "Lenovo";
    }
}

class ASUS implements Computer {
    @Override
    public String name() {
        return "ASUS";
    }
}
