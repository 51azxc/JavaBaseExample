package design.pattern.behavioral.templatemethod;

public abstract class Game {
    abstract void firstHalf();
    abstract void intermission();
    abstract void secondHalf();
    abstract void end();

    public final void play() {
        firstHalf();
        intermission();
        secondHalf();
        end();
    }
}

class Football extends Game {
    @Override
    void firstHalf() {
        System.out.println("The football game is starting!");
    }

    @Override
    void intermission() {
        System.out.println("The first half ended, intermission.");
    }

    @Override
    void secondHalf() {
        System.out.println("The second half is starting!");
    }

    @Override
    void end() {
        System.out.println("Game over");
    }
}

class Basketball extends Game {
    @Override
    void firstHalf() {
        System.out.println("The basketball game is starting!");
    }

    @Override
    void intermission() {
        System.out.println("The first half ended, intermission.");
    }

    @Override
    void secondHalf() {
        System.out.println("The second half is starting!");
    }

    @Override
    void end() {
        System.out.println("Game over");
    }
}
