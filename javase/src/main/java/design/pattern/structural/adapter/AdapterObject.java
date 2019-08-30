package design.pattern.structural.adapter;

public class AdapterObject implements Target{
    private Adaptee adaptee;

    public AdapterObject() {
        this.adaptee = new Adaptee();
    }

    @Override
    public void request() {
        adaptee.specificRequest();
    }
}
