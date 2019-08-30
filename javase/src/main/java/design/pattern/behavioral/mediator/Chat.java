package design.pattern.behavioral.mediator;

public class Chat {
    public static void showMessage(ChatUser user, String message) {
        System.out.println(user.getName() + ": " + message);
    }
}
