package concurrent.producer.and.consumer;

import java.util.concurrent.TimeUnit;

public class WaitAndNotifyAllTest {
	static Integer count = 0;
	static final Integer FULL = 10;
	static String LOCK = "LOCK";
	
	class Producer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (LOCK) {
					while (count == FULL) {
						try {
							LOCK.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					count++;
					System.out.println(Thread.currentThread().getName()+" product: " + count);
					LOCK.notifyAll();
				}
			}
		}
	}
	
	class Consumer implements Runnable {
		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (LOCK) {
					while (count == 0) {
						try {
							LOCK.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					count--;
					System.out.println(Thread.currentThread().getName()+" consume: " + count);
					LOCK.notifyAll();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		WaitAndNotifyAllTest test = new WaitAndNotifyAllTest();
		
		new Thread(test.new Producer()).start();
		new Thread(test.new Consumer()).start();
		new Thread(test.new Producer()).start();
		new Thread(test.new Consumer()).start();
	}
}
