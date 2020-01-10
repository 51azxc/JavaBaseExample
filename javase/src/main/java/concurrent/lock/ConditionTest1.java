package concurrent.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 两个线程交替打印0/1
 */

public class ConditionTest1 {

	private static int total = 1;
	
	public static void main(String[] args) throws Exception {
		Lock lock = new ReentrantLock();
		Condition conditionA = lock.newCondition();
		Condition conditionB = lock.newCondition();
		CountDownLatch latch = new CountDownLatch(2);
		
		System.out.println("START");
		ExecutorService service = Executors.newFixedThreadPool(2);
		service.execute(() -> {
			try {
				lock.lock();
				while (total <= 10) {
					total ++;
					System.out.print(0);
					TimeUnit.MILLISECONDS.sleep(100);
					conditionA.await();
					conditionB.signal();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
				latch.countDown();
			}
		});
		service.execute(() -> {
			try {
				lock.lock();
				while (total <= 10) {
					total ++;
					System.out.print(1);
					TimeUnit.MILLISECONDS.sleep(100);
					conditionA.signal();
					conditionB.await();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
				latch.countDown();
			}
		});
		latch.await();
		System.out.println();
		System.out.println("END");
		service.shutdown();
	}

}
