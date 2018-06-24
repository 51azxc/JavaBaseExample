package concurrent.producer.and.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

	static Integer count = 0;
	static final Integer FULL = 10;
	
	final static Lock lock = new ReentrantLock();
	final static Condition condition1 = lock.newCondition();
	final static Condition condition2 = lock.newCondition();
	
	static class Producer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lock.lock();
				try {
					while (count == FULL) {
						try {
							condition1.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					count++;
					System.out.println(Thread.currentThread().getName()+" product: " + count);
					condition2.signal();
				} finally {
					lock.unlock();
				}
			}
		}
	}
	
	static class Consumer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lock.lock();
				try {
					while (count == 0) {
						try {
							condition2.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					count--;
					System.out.println(Thread.currentThread().getName()+" consume: " + count);
					condition1.signal();
				} finally {
					lock.unlock();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		ExecutorService producerService = Executors.newFixedThreadPool(2);
		ExecutorService consumerService = Executors.newFixedThreadPool(2);
		
		producerService.execute(new Producer());
		consumerService.execute(new Consumer());
		
		producerService.shutdown();
		consumerService.shutdown();
	}

}
