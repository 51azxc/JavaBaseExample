package design.pattern.behavioral.observer;

public abstract class Observer {
    protected Subject subject;
    public abstract void update();
}

class MessageSubscriber1 extends Observer {
    public MessageSubscriber1(Subject subject) {
        this.subject = subject;
        subject.attach(this);
    }

    @Override
    public void update() {
        System.out.println("MessageSubscriber1: " + subject.getState());
    }
}

class MessageSubscriber2 extends Observer {
    public MessageSubscriber2(Subject subject) {
        this.subject = subject;
        subject.attach(this);
    }

    @Override
    public void update() {
        System.out.println("MessageSubscriber2: " + subject.getState());
    }
}
