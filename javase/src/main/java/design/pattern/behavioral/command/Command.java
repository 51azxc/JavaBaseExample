package design.pattern.behavioral.command;

public interface Command {
    void execute();
}

class LightTurnOn implements Command {
    private Light light;

    public LightTurnOn(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }
}

class LightTurnOff implements Command {
    private Light light;

    public LightTurnOff(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOff();
    }
}