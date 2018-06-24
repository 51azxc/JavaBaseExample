package concurrent.producer.and.consumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BlockQueueTest {

	static Integer count = 0;
	static final Integer FULL = 10;
	
	final static BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(10);
	
	static class Producer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
					blockingQueue.put(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count++;
				System.out.println(Thread.currentThread().getName()+" product: " + count);
			}
		}
	}
	
	static class Consumer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
					blockingQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count--;
				System.out.println(Thread.currentThread().getName()+" consume: " + count);
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
