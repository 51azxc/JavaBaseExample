package design.pattern.structural.adapter;

public class AdapterClass extends Adaptee implements Target {
    @Override
    public void request() {
        this.specificRequest();
    }
}
