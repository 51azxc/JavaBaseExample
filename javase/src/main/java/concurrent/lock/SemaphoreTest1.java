package concurrent.lock;

import java.util.concurrent.*;
import java.util.function.IntConsumer;
/*
每个线程都有一个 printNumber 方法来输出一个整数。请修改给出的代码以输出整数序列 010203040506... ，其中序列的长度必须为 2n。
输入：n = 2
输出："0102"
说明：三条线程异步执行，其中一个调用 zero()，另一个线程调用 even()，最后一个线程调用odd()。正确的输出为 "0102"。
 */
public class SemaphoreTest1 {
    public static void main(String[] args) {
        class ZeroEvenOdd {
            private int n;
            private Semaphore semaphore1 = new Semaphore(1);
            private Semaphore semaphore2 = new Semaphore(1);
            private Semaphore semaphore3 = new Semaphore(1);
            public ZeroEvenOdd(int n) {
                this.n = n;
                try {
                    semaphore2.acquire();
                    semaphore3.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            public void zero(IntConsumer printNumber) throws InterruptedException {
                for (int i = 0; i < n; i++) {
                    semaphore1.acquire();
                    printNumber.accept(0);
                    if (i % 2 == 0) {
                        semaphore3.release();
                    } else {
                        semaphore2.release();
                    }
                }
            }
            public void even(IntConsumer printNumber) throws InterruptedException {
                for (int i = 2; i <= n; i += 2) {
                    semaphore2.acquire();
                    printNumber.accept(i);
                    semaphore1.release();
                }
            }

            public void odd(IntConsumer printNumber) throws InterruptedException {
                for (int i = 1; i <= n; i += 2) {
                    semaphore3.acquire();
                    printNumber.accept(i);
                    semaphore1.release();
                }
            }
        }
        ExecutorService service = Executors.newFixedThreadPool(3);
        ZeroEvenOdd object = new ZeroEvenOdd(5);
        IntConsumer consumer = (x) -> System.out.println(x);
        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                object.zero(consumer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                object.even(consumer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
                object.odd(consumer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        service.shutdown();
    }
}
