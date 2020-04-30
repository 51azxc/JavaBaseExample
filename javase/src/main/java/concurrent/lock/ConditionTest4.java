package concurrent.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest4 {
    public static void main(String[] args) {
        class H2O {
            private volatile int h = 0;
            private volatile int o = 0;
            private Lock lock = new ReentrantLock();
            private Condition condition = lock.newCondition();
            public H2O() {
            }
            public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
                try {
                    lock.lock();
                    while (h == 2) {
                        condition.await();
                    }
                    h ++;
                    releaseHydrogen.run();
                    if (h == 2 && o == 1) {
                        h = 0;
                        o = 0;
                    }
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
            public void oxygen(Runnable releaseOxygen) throws InterruptedException {
                try {
                    lock.lock();
                    while (o == 1) {
                        condition.await();
                    }
                    o ++;
                    releaseOxygen.run();
                    if (h == 2 && o == 1) {
                        h = 0;
                        o = 0;
                    }
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
        ExecutorService service = Executors.newCachedThreadPool();
        String[] ss = "HHHHHHHHHHOHHOHHHHOOHHHOOOOHHOOHOHHHHHOOHOHHHOOOOOOHHHHHHHHH".split("");
        H2O h2O = new H2O();
        for (int i = 0; i < ss.length; i++) {
            final String s = ss[i];
            if ("H".equals(s)) {
                service.execute(() -> {
                    try {
                        h2O.hydrogen(() -> System.out.print(s));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else if ("O".equals(ss[i])) {
                service.execute(() -> {
                    try {
                        h2O.oxygen(() -> System.out.print(s));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        service.shutdown();
    }
}
