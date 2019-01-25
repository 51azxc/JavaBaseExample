package concurrent.producer.and.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreTest {
	
	static Integer count = 0;
	static final Integer FULL = 10;
	
	final static Semaphore semaphore1 = new Semaphore(10);
	final static Semaphore semaphore2 = new Semaphore(0);
	final static Semaphore mutex = new Semaphore(1);
	
	static class Producer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
					semaphore1.acquire();
					mutex.acquire();
					count++;
					System.out.println(Thread.currentThread().getName()+" product: " + count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					mutex.release();
					semaphore2.release();
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
					semaphore2.acquire();
					mutex.acquire();
					count--;
					System.out.println(Thread.currentThread().getName()+" consume: " + count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					mutex.release();
					semaphore1.release();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		ExecutorService producerService = Executors.newFixedThreadPool(2);
		ExecutorService consumerService = Executors.newFixedThreadPool(2);
		
		producerService.execute(new Producer());
		producerService.execute(new Producer());
		consumerService.execute(new Consumer());
		consumerService.execute(new Consumer());
		
		producerService.shutdown();
		consumerService.shutdown();
	}

}
