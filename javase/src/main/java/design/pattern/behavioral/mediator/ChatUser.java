package design.pattern.behavioral.mediator;

public class ChatUser {
    private String name;

    public ChatUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) {
        Chat.showMessage(this, message);
    }
}
