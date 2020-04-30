package concurrent.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 请设计修改程序，以确保 two() 方法在 one() 方法之后被执行，three() 方法在 two() 方法之后被执行。
 */
public class ConditionTest2 {
    public static void main(String[] args) {
        class Foo {
            private volatile Integer count = 1;
            private String LOCK1 = "LOCK1";
            public Foo() {

            }

            public void first(Runnable printFirst) throws InterruptedException {
                synchronized (LOCK1) {
                    while (count != 1) {
                        LOCK1.wait();
                    }
                    printFirst.run();
                    count = 2;
                    LOCK1.notifyAll();
                }
            }

            public void second(Runnable printSecond) throws InterruptedException {
                synchronized (LOCK1) {
                    while (count != 2) {
                        LOCK1.wait();
                    }
                    printSecond.run();
                    count = 3;
                    LOCK1.notifyAll();
                }
            }

            public void third(Runnable printThird) throws InterruptedException {
                synchronized (LOCK1) {
                    while (count != 3) {
                        LOCK1.wait();
                    }
                    printThird.run();
                    count = 1;
                    LOCK1.notifyAll();
                }
            }
        }

        Runnable runnable1 = () -> System.out.println("one");
        Runnable runnable2 = () -> System.out.println("two");
        Runnable runnable3 = () -> System.out.println("three");
        ExecutorService service = Executors.newFixedThreadPool(3);
        Foo foo = new Foo();

        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                foo.first(runnable1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                foo.third(runnable3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                foo.second(runnable2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.shutdown();

    }
}
