package design.pattern.behavioral.command;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private List<Command> list = new ArrayList<>();

    public void setCommand(Command command) {
        list.add(command);
    }

    public void excute() {
        list.forEach(command -> command.execute());
        list.clear();
    }
}
