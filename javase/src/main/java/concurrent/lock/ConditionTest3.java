package concurrent.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest3 {
    public static void main(String[] args) {
        class FooBar {
            private int n;
            private Lock lock = new ReentrantLock();
            private Condition condition1 = lock.newCondition();
            private Condition condition2 = lock.newCondition();
            public FooBar(int n) {
                this.n = n;
            }
            public void foo(Runnable printFoo) throws InterruptedException {
                try {
                    lock.lock();
                    for (int i = 0; i < n; i++) {
                        printFoo.run();
                        condition1.await();
                        condition2.signal();
                    }
                } finally {
                    lock.unlock();
                }
            }

            public void bar(Runnable printBar) throws InterruptedException {
                try {
                    lock.lock();
                    for (int i = 0; i < n; i++) {
                        printBar.run();
                        condition1.signal();
                        condition2.await();
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(2);
        FooBar fooBar = new FooBar(3);
        service.execute(() -> {
            try {
                fooBar.foo(() -> System.out.print("foo"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                fooBar.bar(() -> System.out.print("bar"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.shutdown();
    }
}
